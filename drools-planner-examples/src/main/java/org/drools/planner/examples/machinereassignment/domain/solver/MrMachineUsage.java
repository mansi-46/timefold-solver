/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.examples.machinereassignment.domain.solver;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.examples.machinereassignment.domain.MrMachine;
import org.drools.planner.examples.machinereassignment.domain.MrMachineCapacity;
import org.drools.planner.examples.machinereassignment.domain.MrResource;

public class MrMachineUsage implements Serializable {

    private MrMachineCapacity machineCapacity;
    private int usage;

    public MrMachineUsage(MrMachineCapacity machineCapacity, int usage) {
        this.machineCapacity = machineCapacity;
        this.usage = usage;
    }

    public MrMachineCapacity getMachineCapacity() {
        return machineCapacity;
    }

    public int getUsage() {
        return usage;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MrMachineUsage) {
            MrMachineUsage other = (MrMachineUsage) o;
            return new EqualsBuilder()
                    .append(machineCapacity, other.machineCapacity)
                    .append(usage, other.usage)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(machineCapacity)
                .append(usage)
                .toHashCode();
    }

    public MrMachine getMachine() {
        return machineCapacity.getMachine();
    }

    public MrResource getResource() {
        return machineCapacity.getResource();
    }

    public int getMaximumAvailable() {
        return machineCapacity.getMaximumCapacity() - usage;
    }

    @Override
    public String toString() {
        return getMachine() + "-" + getResource() + "=" + usage;
    }

}
