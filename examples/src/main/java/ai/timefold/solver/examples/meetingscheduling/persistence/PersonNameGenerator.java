package ai.timefold.solver.examples.meetingscheduling.persistence;

import ai.timefold.solver.examples.common.persistence.generator.StringDataGenerator;

public class PersonNameGenerator {
    private final StringDataGenerator fullNameGenerator = StringDataGenerator.buildFullNames();

    public String generateFullName() {
        return fullNameGenerator.generateNextValue();
    }
}
