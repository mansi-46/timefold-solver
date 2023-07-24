package ai.timefold.solver.examples.conferencescheduling.domain;

import java.util.HashSet;
import java.util.Set;

public class SpeakerTags {

    private Set<String> requiredTimeslotTagSet;
    private Set<String> preferredTimeslotTagSet;
    private Set<String> prohibitedTimeslotTagSet;
    private Set<String> undesiredTimeslotTagSet;
    private Set<String> requiredRoomTagSet;
    private Set<String> preferredRoomTagSet;
    private Set<String> prohibitedRoomTagSet;
    private Set<String> undesiredRoomTagSet;

    public SpeakerTags() {
        requiredTimeslotTagSet = new HashSet<>();
        preferredTimeslotTagSet = new HashSet<>();
        prohibitedTimeslotTagSet = new HashSet<>();
        undesiredTimeslotTagSet = new HashSet<>();
        requiredRoomTagSet = new HashSet<>();
        preferredRoomTagSet = new HashSet<>();
        prohibitedRoomTagSet = new HashSet<>();
        undesiredRoomTagSet = new HashSet<>();
    }

    public Set<String> getRequiredTimeslotTagSet() {
        return requiredTimeslotTagSet;
    }

    public Set<String> getPreferredTimeslotTagSet() {
        return preferredTimeslotTagSet;
    }

    public Set<String> getProhibitedTimeslotTagSet() {
        return prohibitedTimeslotTagSet;
    }

    public Set<String> getUndesiredTimeslotTagSet() {
        return undesiredTimeslotTagSet;
    }

    public Set<String> getRequiredRoomTagSet() {
        return requiredRoomTagSet;
    }

    public Set<String> getPreferredRoomTagSet() {
        return preferredRoomTagSet;
    }

    public Set<String> getProhibitedRoomTagSet() {
        return prohibitedRoomTagSet;
    }

    public Set<String> getUndesiredRoomTagSet() {
        return undesiredRoomTagSet;
    }

    public void addTag(String tag) {
        if (tag == null) {
            return;
        }
        if (tag.startsWith("requiredTimeslotTag_")) {
            requiredTimeslotTagSet.add(tag);
        } else if (tag.startsWith("preferredTimeslotTag_")) {
            preferredTimeslotTagSet.add(tag);
        } else if (tag.startsWith("prohibitedTimeslotTag_")) {
            prohibitedTimeslotTagSet.add(tag);
        } else if (tag.startsWith("undesiredTimeslotTag_")) {
            undesiredTimeslotTagSet.add(tag);
        } else if (tag.startsWith("requiredRoomTag_")) {
            requiredRoomTagSet.add(tag);
        } else if (tag.startsWith("preferredRoomTag_")) {
            preferredRoomTagSet.add(tag);
        } else if (tag.startsWith("prohibitedRoomTag_")) {
            prohibitedRoomTagSet.add(tag);
        } else if (tag.startsWith("undesiredRoomTag_")) {
            undesiredRoomTagSet.add(tag);
        }
    }
}
