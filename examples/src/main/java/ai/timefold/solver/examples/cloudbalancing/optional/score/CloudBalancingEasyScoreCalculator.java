package ai.timefold.solver.examples.cloudbalancing.optional.score;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import ai.timefold.solver.examples.cloudbalancing.domain.CloudBalance;
import ai.timefold.solver.examples.cloudbalancing.domain.CloudComputer;
import ai.timefold.solver.examples.cloudbalancing.domain.CloudProcess;

public class CloudBalancingEasyScoreCalculator implements EasyScoreCalculator<CloudBalance, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(CloudBalance cloudBalance) {
        int hardScore = calculateHardScore(cloudBalance);
        int softScore = calculateSoftScore(cloudBalance);
        return HardSoftScore.of(hardScore, softScore);
    }

    private int calculateHardScore(CloudBalance cloudBalance) {
        int hardScore = 0;
        for (CloudComputer computer : cloudBalance.getComputerList()) {
            int cpuPowerUsage = 0;
            int memoryUsage = 0;
            int networkBandwidthUsage = 0;

            // Calculate usage
            for (CloudProcess process : cloudBalance.getProcessList()) {
                if (computer.equals(process.getComputer())) {
                    cpuPowerUsage += process.getRequiredCpuPower();
                    memoryUsage += process.getRequiredMemory();
                    networkBandwidthUsage += process.getRequiredNetworkBandwidth();
                }
            }

            // Hard constraints
            int cpuPowerAvailable = computer.getCpuPower() - cpuPowerUsage;
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
            int memoryAvailable = computer.getMemory() - memoryUsage;
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
            int networkBandwidthAvailable = computer.getNetworkBandwidth() - networkBandwidthUsage;
            if (networkBandwidthAvailable < 0) {
                hardScore += networkBandwidthAvailable;
            }
        }
        return hardScore;
    }

    private int calculateSoftScore(CloudBalance cloudBalance) {
        int softScore = 0;
        for (CloudComputer computer : cloudBalance.getComputerList()) {
            boolean used = false;
            for (CloudProcess process : cloudBalance.getProcessList()) {
                if (computer.equals(process.getComputer())) {
                    used = true;
                    break;
                }
            }
            // Soft constraints
            if (used) {
                softScore -= computer.getCost();
            }
        }
        return softScore;
    }
}
