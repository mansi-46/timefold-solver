package ai.timefold.solver.examples.taskassigning.swingui;

import static ai.timefold.solver.examples.taskassigning.persistence.TaskAssigningGenerator.BASE_DURATION_AVERAGE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

import ai.timefold.solver.examples.common.swingui.SolutionPanel;
import ai.timefold.solver.examples.taskassigning.domain.Customer;
import ai.timefold.solver.examples.taskassigning.domain.Priority;
import ai.timefold.solver.examples.taskassigning.domain.Task;
import ai.timefold.solver.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.examples.taskassigning.domain.TaskType;

public class TaskAssigningPanel extends SolutionPanel<TaskAssigningSolution> {

    public static final String LOGO_PATH = "/ai/timefold/solver/examples/taskassigning/swingui/taskAssigningLogo.png";

    private final TaskOverviewPanel taskOverviewPanel;

    private JSpinner consumeRateField;
    private AbstractAction consumeAction;
    private Timer consumeTimer;
    private JSpinner produceRateField;
    private AbstractAction produceAction;
    private Timer produceTimer;

    private int consumedTimeInSeconds = 0;
    private int previousConsumedTime = 0; // In minutes
    private int producedTimeInSeconds = 0;
    private int previousProducedTime = 0; // In minutes
    private volatile Random producingRandom;

    public TaskAssigningPanel() {
        setLayout(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        taskOverviewPanel = new TaskOverviewPanel(this);
        add(new JScrollPane(taskOverviewPanel), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new GridLayout(1, 0));
        JPanel consumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        consumePanel.add(new JLabel("Consume rate:"));
        consumeRateField = new JSpinner(new SpinnerNumberModel(600, 10, 3600, 10));
        consumePanel.add(consumeRateField);
        consumeTimer = new Timer(1000, e -> {
            consumedTimeInSeconds += (Integer) consumeRateField.getValue();
            consumeUpTo(consumedTimeInSeconds / 60);
            repaint();
        });
        consumeAction = new AbstractAction("Consume") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!consumeTimer.isRunning()) {
                    consumeRateField.setEnabled(false);
                    consumeTimer.start();
                } else {
                    consumeRateField.setEnabled(true);
                    consumeTimer.stop();
                }
            }
        };
        consumePanel.add(new JToggleButton(consumeAction));
        // FIXME remove this when https://issues.redhat.com/browse/PLANNER-2633 is done.
        Arrays.stream(consumePanel.getComponents()).forEach(component -> {
            component.setEnabled(false);
            if (component instanceof JComponent) {
                ((JComponent) component).setToolTipText("This feature is currently disabled.");
            }
        });
        headerPanel.add(consumePanel);
        JPanel producePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        producePanel.add(new JLabel("Produce rate:"));
        produceRateField = new JSpinner(new SpinnerNumberModel(600, 10, 3600, 10));
        producePanel.add(produceRateField);
        produceTimer = new Timer(1000, e -> {
            producedTimeInSeconds += (Integer) produceRateField.getValue();
            produceUpTo(producedTimeInSeconds / 60);
            repaint();
        });
        produceAction = new AbstractAction("Produce") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!produceTimer.isRunning()) {
                    produceRateField.setEnabled(false);
                    produceTimer.start();
                } else {
                    produceRateField.setEnabled(true);
                    produceTimer.stop();
                }
            }
        };
        producePanel.add(new JToggleButton(produceAction));
        headerPanel.add(producePanel);
        return headerPanel;
    }

    /**
     * @param consumedTime in minutes, just like {@link Task#getStartTime()}
     */
    public void consumeUpTo(final int consumedTime) {
        taskOverviewPanel.setConsumedDuration(consumedTime);
        if (consumedTime <= previousConsumedTime) {
            // Occurs due to rounding down of consumedTimeInSeconds
            return;
        }
        logger.debug("Scheduling consumption of all tasks up to {} minutes.", consumedTime);
        previousConsumedTime = consumedTime;
        doProblemChange((taskAssigningSolution, problemChangeDirector) -> {
            taskAssigningSolution.setFrozenCutoff(consumedTime);
            // TODO update list variable pins: https://issues.redhat.com/browse/PLANNER-2633.
        });
    }

    /**
     * @param producedTime in minutes, just like {@link Task#getStartTime()}
     */
    public void produceUpTo(final int producedTime) {
        if (producedTime <= previousProducedTime) {
            // Occurs due to rounding down of producedDurationInSeconds
            return;
        }
        final int baseDurationBudgetPerEmployee = (producedTime - previousProducedTime);
        final int newTaskCount = calculateNewTaskCount(baseDurationBudgetPerEmployee);
        if (newTaskCount <= 0) {
            // Do not change previousProducedDuration
            return;
        }
        logger.debug("Scheduling production of {} new tasks.", newTaskCount);
        previousProducedTime = producedTime;
        scheduleProduction(newTaskCount);
    }

    /**
     * Calculate the number of new tasks to be produced based on the given base duration budget per employee.
     *
     * @param baseDurationBudgetPerEmployee The base duration budget per employee for which new tasks are to be produced.
     * @return The number of new tasks to be produced.
     */
    private int calculateNewTaskCount(int baseDurationBudgetPerEmployee) {
        return getSolution().getEmployeeList().size() * baseDurationBudgetPerEmployee / BASE_DURATION_AVERAGE;
    }

    /**
     * Schedule the production of new tasks and update the problem with the created tasks.
     *
     * @param newTaskCount The number of new tasks to be produced.
     */
    private void scheduleProduction(int newTaskCount) {
        final int readyTime = previousConsumedTime;
        doProblemChange((taskAssigningSolution, problemChangeDirector) -> {
            List<TaskType> taskTypeList = taskAssigningSolution.getTaskTypeList();
            List<Customer> customerList = taskAssigningSolution.getCustomerList();
            Priority[] priorities = Priority.values();
            List<Task> taskList = taskAssigningSolution.getTaskList();
            for (int i = 0; i < newTaskCount; i++) {
                Task task = createNewTask(taskTypeList, customerList, priorities, readyTime);
                problemChangeDirector.addEntity(task, taskList::add);
            }
        });
    }

    /**
     * Create a new task with random attributes.
     *
     * @param taskTypeList The list of available task types.
     * @param customerList The list of available customers.
     * @param priorities   The array of task priorities.
     * @param readyTime    The ready time for the new task.
     * @return The newly created task.
     */
    private Task createNewTask(List<TaskType> taskTypeList, List<Customer> customerList,
                               Priority[] priorities, int readyTime) {
        TaskType taskType = taskTypeList.get(producingRandom.nextInt(taskTypeList.size()));
        long nextTaskId = findNextTaskId();
        int nextIndexInTaskType = findNextIndexInTaskType(taskType);
        Customer customer = customerList.get(producingRandom.nextInt(customerList.size()));
        Priority priority = priorities[producingRandom.nextInt(priorities.length)];
        return new Task(nextTaskId, taskType, nextIndexInTaskType, customer, readyTime, priority);
    }


    /**
     * Find the next available task ID based on the existing tasks in the solution.
     *
     * @return The next available task ID.
     */
    private long findNextTaskId() {
        long nextTaskId = 0L;
        for (Task other : getSolution().getTaskList()) {
            if (nextTaskId <= other.getId()) {
                nextTaskId = other.getId() + 1L;
            }
        }
        return nextTaskId;
    }

    /**
     * Find the next available index in the task type based on the existing tasks in the solution.
     *
     * @param taskType The task type for which the next available index is to be found.
     * @return The next available index in the task type.
     */
    private int findNextIndexInTaskType(TaskType taskType) {
        int nextIndexInTaskType = 0;
        for (Task other : getSolution().getTaskList()) {
            if (taskType == other.getTaskType()) {
                if (nextIndexInTaskType <= other.getIndexInTaskType()) {
                    nextIndexInTaskType = other.getIndexInTaskType() + 1;
                }
            }
        }
        return nextIndexInTaskType;
    }

    @Override
    public boolean isWrapInScrollPane() {
        return false;
    }

    @Override
    public void resetPanel(TaskAssigningSolution solution) {
        consumedTimeInSeconds = solution.getFrozenCutoff() * 60;
        previousConsumedTime = solution.getFrozenCutoff();
        producedTimeInSeconds = 0;
        previousProducedTime = 0;
        producingRandom = new Random(0); // Random is thread safe
        taskOverviewPanel.resetPanel(solution);
        taskOverviewPanel.setConsumedDuration(consumedTimeInSeconds / 60);
    }

    @Override
    public void updatePanel(TaskAssigningSolution taskAssigningSolution) {
        taskOverviewPanel.resetPanel(taskAssigningSolution);
    }

}
