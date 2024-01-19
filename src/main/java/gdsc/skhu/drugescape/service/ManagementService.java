package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.ManagementDTO;
import gdsc.skhu.drugescape.domain.model.Management;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.ManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagementService {
    private final ManagementRepository managementRepository;
    private final ReportService reportService;

    public ManagementService(ManagementRepository managementRepository, ReportService reportService) {
        this.managementRepository = managementRepository;
        this.reportService = reportService;
    }

    @Transactional
    public void processManagementRecord(ManagementDTO managementDTO, Long memberId) {
        int totalPoints = calculateTotalPoints(managementDTO);
        int completedTasks = calculateCompletedTasks(managementDTO);
        int dailyGoals = calculateDailyGoals(completedTasks);
        Report report = reportService.modifyOrCreateReport(memberId, totalPoints, managementDTO.isStopDrug() ? 1 : 0, dailyGoals);
        Management management = Management.builder()
                .stopDrug(managementDTO.isStopDrug())
                .exercise(managementDTO.isExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .build();
        managementRepository.save(management);
    }

    private int calculateTotalPoints(ManagementDTO managementDTO) {
        return (managementDTO.isStopDrug() ? 100 : 0) +
                (managementDTO.isExercise() ? 100 : 0) +
                (managementDTO.getMeal() >= 2 ? 100 : 0) +
                (managementDTO.getMedication() != 0 ? 100 : 0);
    }

    private int calculateCompletedTasks(ManagementDTO managementDTO) {
        return (managementDTO.isStopDrug() ? 1 : 0) +
                (managementDTO.isExercise() ? 1 : 0) +
                (managementDTO.getMeal() >= 2 ? 1 : 0) +
                (managementDTO.getMedication() != 0 ? 1 : 0);
    }

    private int calculateDailyGoals(int completedTasks) {
        return 25 * completedTasks;
    }
}