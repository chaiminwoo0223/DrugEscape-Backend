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
        int totalPoints = calculatePoints(managementDTO);
        int completedTasks = calculateCompletedTasks(managementDTO);
        int dailyGoals = 25 * completedTasks;
        Report report = reportService.modifyReport(memberId, totalPoints, managementDTO.isStopDrug() ? 1 : 0, dailyGoals);
        Management management = Management.builder()
                .stopDrug(managementDTO.isStopDrug())
                .exercise(managementDTO.isExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .build();
        managementRepository.save(management);
    }

    private int calculatePoints(ManagementDTO managementDTO) {
        int pointsFromStopDrug = managementDTO.isStopDrug() ? 100 : 0;
        int pointsFromExercise = managementDTO.isExercise() ? 100 : 0;
        int pointsFromMeal = managementDTO.getMeal() >= 2 ? 100 : 0;
        int pointsFromMedication = managementDTO.getMedication() != 0 ? 100 : 0;
        return pointsFromStopDrug + pointsFromExercise + pointsFromMeal + pointsFromMedication;
    }

    private int calculateCompletedTasks(ManagementDTO managementDTO) {
        int tasks = 0;
        if (managementDTO.isStopDrug()) tasks++;
        if (managementDTO.isExercise()) tasks++;
        if (managementDTO.getMeal() >= 2) tasks++;
        if (managementDTO.getMedication() != 0) tasks++;
        return tasks;
    }
}