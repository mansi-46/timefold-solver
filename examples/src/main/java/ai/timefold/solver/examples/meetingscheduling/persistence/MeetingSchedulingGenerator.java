package ai.timefold.solver.examples.meetingscheduling.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.timefold.solver.examples.common.app.CommonApp;
import ai.timefold.solver.examples.common.app.LoggingMain;
import ai.timefold.solver.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.examples.common.persistence.generator.StringDataGenerator;
import ai.timefold.solver.examples.meetingscheduling.app.MeetingSchedulingApp;
import ai.timefold.solver.examples.meetingscheduling.domain.Attendance;
import ai.timefold.solver.examples.meetingscheduling.domain.Day;
import ai.timefold.solver.examples.meetingscheduling.domain.Meeting;
import ai.timefold.solver.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.examples.meetingscheduling.domain.MeetingConstraintConfiguration;
import ai.timefold.solver.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.examples.meetingscheduling.domain.Person;
import ai.timefold.solver.examples.meetingscheduling.domain.PreferredAttendance;
import ai.timefold.solver.examples.meetingscheduling.domain.RequiredAttendance;
import ai.timefold.solver.examples.meetingscheduling.domain.Room;
import ai.timefold.solver.examples.meetingscheduling.domain.TimeGrain;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class MeetingSchedulingGenerator extends LoggingMain {

    public static void main(String[] args) {
        MeetingSchedulingGenerator generator = new MeetingSchedulingGenerator();
        generator.writeMeetingSchedule(50, 5);
        generator.writeMeetingSchedule(100, 5);
        generator.writeMeetingSchedule(200, 5);
        generator.writeMeetingSchedule(400, 5);
        generator.writeMeetingSchedule(800, 5);
    }


    private final StringDataGenerator topicGenerator = new StringDataGenerator()
            .addPart(true, 0,
                    "Strategize",
                    "Fast track",
                    "Cross sell",
                    "Profitize",
                    "Transform",
                    "Engage",
                    "Downsize",
                    "Ramp up",
                    "On board",
                    "Reinvigorate")
            .addPart(false, 1,
                    "data driven",
                    "sales driven",
                    "compelling",
                    "reusable",
                    "negotiated",
                    "sustainable",
                    "laser-focused",
                    "flexible",
                    "real-time",
                    "targeted")
            .addPart(true, 1,
                    "B2B",
                    "e-business",
                    "virtualization",
                    "multitasking",
                    "one stop shop",
                    "braindumps",
                    "data mining",
                    "policies",
                    "synergies",
                    "user experience")
            .addPart(false, 3,
                    "in a nutshell",
                    "in practice",
                    "for dummies",
                    "in action",
                    "recipes",
                    "on the web",
                    "for decision makers",
                    "on the whiteboard",
                    "out of the box",
                    "in the new economy");

    private final int[] durationInGrainsOptions = {
            1, // 15 mins
            2, // 30 mins
            3, // 45 mins
            4, // 1 hour
            6, // 90 mins
            8, // 2 hours
            16, // 4 hours
    };

    private final int[] personsPerMeetingOptions = {
            2,
            3,
            4,
            5,
            6,
            8,
            10,
            12,
            14,
            16,
            20,
            30,
    };

    private final int[] startingMinuteOfDayOptions = {
            8 * 60, // 08:00
            8 * 60 + 15, // 08:15
            8 * 60 + 30, // 08:30
            8 * 60 + 45, // 08:45
            9 * 60, // 09:00
            9 * 60 + 15, // 09:15
            9 * 60 + 30, // 09:30
            9 * 60 + 45, // 09:45
            10 * 60, // 10:00
            10 * 60 + 15, // 10:15
            10 * 60 + 30, // 10:30
            10 * 60 + 45, // 10:45
            11 * 60, // 11:00
            11 * 60 + 15, // 11:15
            11 * 60 + 30, // 11:30
            11 * 60 + 45, // 11:45
            13 * 60, // 13:00
            13 * 60 + 15, // 13:15
            13 * 60 + 30, // 13:30
            13 * 60 + 45, // 13:45
            14 * 60, // 14:00
            14 * 60 + 15, // 14:15
            14 * 60 + 30, // 14:30
            14 * 60 + 45, // 14:45
            15 * 60, // 15:00
            15 * 60 + 15, // 15:15
            15 * 60 + 30, // 15:30
            15 * 60 + 45, // 15:45
            16 * 60, // 16:00
            16 * 60 + 15, // 16:15
            16 * 60 + 30, // 16:30
            16 * 60 + 45, // 16:45
            17 * 60, // 17:00
            17 * 60 + 15, // 17:15
            17 * 60 + 30, // 17:30
            17 * 60 + 45, // 17:45
    };

    private final PersonNameGenerator personNameGenerator = new PersonNameGenerator();

    protected final SolutionFileIO<MeetingSchedule> solutionFileIO;
    protected final File outputDir;

    protected Random random;

    public MeetingSchedulingGenerator() {
        solutionFileIO = new MeetingSchedulingXlsxFileIO();
        outputDir = new File(CommonApp.determineDataDir(MeetingSchedulingApp.DATA_DIR_NAME), "unsolved");
    }

    private void writeMeetingSchedule(int meetingListSize, int roomListSize) {
        int timeGrainListSize = meetingListSize * durationInGrainsOptions[durationInGrainsOptions.length - 1] / roomListSize;
        String fileName = determineFileName(meetingListSize, timeGrainListSize, roomListSize);
        File outputFile = new File(outputDir, fileName + "." + solutionFileIO.getOutputFileExtension());
        MeetingSchedule meetingSchedule = createMeetingSchedule(fileName, meetingListSize, timeGrainListSize, roomListSize);
        solutionFileIO.write(meetingSchedule, outputFile);
        logger.info("Saved: {}", outputFile);
    }

    private String determineFileName(int meetingListSize, int timeGrainListSize, int roomListSize) {
        return meetingListSize + "meetings-" + timeGrainListSize + "timegrains-" + roomListSize + "rooms";
    }

    public MeetingSchedule createMeetingSchedule(String fileName, int meetingListSize, int timeGrainListSize,
                                                 int roomListSize) {
        random = new Random(37);
        MeetingSchedule meetingSchedule = new MeetingSchedule(0L);
        MeetingConstraintConfiguration constraintConfiguration = new MeetingConstraintConfiguration(0L);
        meetingSchedule.setConstraintConfiguration(constraintConfiguration);

        createMeetingListAndAttendanceList(meetingSchedule, meetingListSize);
        createTimeGrainList(meetingSchedule, timeGrainListSize);
        createRoomList(meetingSchedule, roomListSize);
        createPersonList(meetingSchedule);
        linkAttendanceListToPersons(meetingSchedule);
        createMeetingAssignmentList(meetingSchedule);

        BigInteger possibleSolutionSize = BigInteger.valueOf((long) timeGrainListSize * roomListSize)
                .pow(meetingSchedule.getMeetingAssignmentList().size());
        logger.info("MeetingSchedule {} has {} meetings, {} timeGrains and {} rooms with a search space of {}.", fileName,
                meetingListSize, timeGrainListSize, roomListSize,
                AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return meetingSchedule;
    }

    private void createMeetingListAndAttendanceList(MeetingSchedule meetingSchedule, int meetingListSize) {
        List<Meeting> meetingList = new ArrayList<>(meetingListSize);
        List<Attendance> globalAttendanceList = new ArrayList<>();
        long attendanceId = 0L;
        topicGenerator.predictMaximumSizeAndReset(meetingListSize);
        for (int i = 0; i < meetingListSize; i++) {
            String topic = topicGenerator.generateNextValue();
            int durationInGrains = durationInGrainsOptions[random.nextInt(durationInGrainsOptions.length)];
            int attendanceListSize = personsPerMeetingOptions[random.nextInt(personsPerMeetingOptions.length)];
            int requiredAttendanceListSize = Math.max(2, random.nextInt(attendanceListSize + 1));
            Meeting meeting = new Meeting(i, topic, durationInGrains);
            List<RequiredAttendance> requiredAttendanceList = new ArrayList<>(requiredAttendanceListSize);
            for (int j = 0; j < requiredAttendanceListSize; j++) {
                RequiredAttendance attendance = new RequiredAttendance(attendanceId++, meeting);
                // person is filled in later
                requiredAttendanceList.add(attendance);
                globalAttendanceList.add(attendance);
            }
            meeting.setRequiredAttendanceList(requiredAttendanceList);
            int preferredAttendanceListSize = attendanceListSize - requiredAttendanceListSize;
            List<PreferredAttendance> preferredAttendanceList = new ArrayList<>(preferredAttendanceListSize);
            for (int j = 0; j < preferredAttendanceListSize; j++) {
                PreferredAttendance attendance = new PreferredAttendance(attendanceId++, meeting);
                // person is filled in later
                preferredAttendanceList.add(attendance);
                globalAttendanceList.add(attendance);
            }
            meeting.setPreferredAttendanceList(preferredAttendanceList);

            logger.trace("Created meeting with topic ({}), durationInGrains ({}), requiredAttendanceListSize ({}), " +
                            "preferredAttendanceListSize ({}).", topic, durationInGrains, requiredAttendanceListSize,
                    preferredAttendanceListSize);
            meetingList.add(meeting);
        }
        meetingSchedule.setMeetingList(meetingList);
        meetingSchedule.setAttendanceList(globalAttendanceList);
    }

    private void createTimeGrainList(MeetingSchedule meetingSchedule, int timeGrainListSize) {
        List<Day> dayList = new ArrayList<>(timeGrainListSize);
        long dayId = 0;
        Day day = null;
        List<TimeGrain> timeGrainList = new ArrayList<>(timeGrainListSize);
        for (int i = 0; i < timeGrainListSize; i++) {
            int dayOfYear = (i / startingMinuteOfDayOptions.length) + 1;
            if (day == null || day.getDayOfYear() != dayOfYear) {
                day = new Day(dayId, dayOfYear);
                dayId++;
                dayList.add(day);
            }
            int startingMinuteOfDay = startingMinuteOfDayOptions[i % startingMinuteOfDayOptions.length];
            TimeGrain timeGrain = new TimeGrain(i, i, day, startingMinuteOfDay);
            logger.trace("Created timeGrain with grainIndex ({}), dayOfYear ({}), startingMinuteOfDay ({}).", i, dayOfYear,
                    startingMinuteOfDay);
            timeGrainList.add(timeGrain);
        }
        meetingSchedule.setDayList(dayList);
        meetingSchedule.setTimeGrainList(timeGrainList);
    }

    private void createRoomList(MeetingSchedule meetingSchedule, int roomListSize) {
        final int roomsPerFloor = 20;
        List<Room> roomList = new ArrayList<>(roomListSize);
        for (int i = 0; i < roomListSize; i++) {
            String name = "R " + ((i / roomsPerFloor * 100) + (i % roomsPerFloor) + 1);
            int capacityOptionsSubsetSize = personsPerMeetingOptions.length * 3 / 4;
            int capacity = personsPerMeetingOptions[personsPerMeetingOptions.length - (i % capacityOptionsSubsetSize) - 1];
            Room room = new Room(i, name, capacity);
            logger.trace("Created room with name ({}), capacity ({}).", name, capacity);
            roomList.add(room);
        }
        meetingSchedule.setRoomList(roomList);
    }

    private void createPersonList(MeetingSchedule meetingSchedule) {
        int attendanceListSize = 0;
        for (Meeting meeting : meetingSchedule.getMeetingList()) {
            attendanceListSize += meeting.getRequiredAttendanceList().size()
                    + meeting.getPreferredAttendanceList().size();
        }
        int personListSize = attendanceListSize * meetingSchedule.getRoomList().size() * 3
                / (4 * meetingSchedule.getMeetingList().size());
        List<Person> personList = new ArrayList<>(personListSize);
        // Remove the fullNameGenerator call and use the personNameGenerator instead:
        for (int i = 0; i < personListSize; i++) {
            String fullName = personNameGenerator.generateFullName();
            Person person = new Person(i, fullName);
            logger.trace("Created person with fullName ({}).", fullName);
            personList.add(person);
        }
        meetingSchedule.setPersonList(personList);
    }

    private void linkAttendanceListToPersons(MeetingSchedule meetingSchedule) {
        for (Meeting meeting : meetingSchedule.getMeetingList()) {
            List<Person> availablePersonList = new ArrayList<>(meetingSchedule.getPersonList());
            int attendanceListSize = meeting.getRequiredAttendanceList().size() + meeting.getPreferredAttendanceList().size();
            if (availablePersonList.size() < attendanceListSize) {
                throw new IllegalStateException("The availablePersonList size (" + availablePersonList.size()
                        + ") is less than the attendanceListSize (" + attendanceListSize + ").");
            }
            for (RequiredAttendance requiredAttendance : meeting.getRequiredAttendanceList()) {
                requiredAttendance.setPerson(availablePersonList.remove(random.nextInt(availablePersonList.size())));
            }
            for (PreferredAttendance preferredAttendance : meeting.getPreferredAttendanceList()) {
                preferredAttendance.setPerson(availablePersonList.remove(random.nextInt(availablePersonList.size())));
            }
        }
    }

    private void createMeetingAssignmentList(MeetingSchedule meetingSchedule) {
        List<Meeting> meetingList = meetingSchedule.getMeetingList();
        List<MeetingAssignment> meetingAssignmentList = new ArrayList<>(meetingList.size());
        for (Meeting meeting : meetingList) {
            MeetingAssignment meetingAssignment = new MeetingAssignment(meeting.getId(), meeting);
            meetingAssignmentList.add(meetingAssignment);
        }
        meetingSchedule.setMeetingAssignmentList(meetingAssignmentList);
    }

}
