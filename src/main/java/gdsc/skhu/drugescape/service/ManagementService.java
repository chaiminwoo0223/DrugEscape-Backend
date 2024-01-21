package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.ManagementDTO;
import gdsc.skhu.drugescape.domain.dto.ReportDTO;
import gdsc.skhu.drugescape.domain.model.Management;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.ManagementRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagementService {
    private final ManagementRepository managementRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;

    public ManagementService(ManagementRepository managementRepository,
                             MemberRepository memberRepository,
                             ReportRepository reportRepository,
                             ReportService reportService) {
        this.managementRepository = managementRepository;
        this.memberRepository = memberRepository;
        this.reportRepository = reportRepository;
        this.reportService = reportService;
    }

    @Transactional
    public void processManagementRecord(Long memberId, ManagementDTO managementDTO) {
        Report report = processReport(memberId, managementDTO);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        Management management = managementRepository.findByMemberId(memberId)
                .orElse(Management.builder().member(member).build());
        updateManagement(management, managementDTO, report);
    }

    private Report processReport(Long memberId, ManagementDTO managementDTO) {
        int totalPoints = calculatePoints(managementDTO);
        int completedTasks = calculateCompletedTasks(managementDTO);
        int dailyGoals = 25 * completedTasks;
        return reportRepository.findByMemberId(memberId)
                .map(existingReport -> reportService.modifyReport(memberId, totalPoints, managementDTO.isStopDrug() ? 1 : 0, dailyGoals))
                .orElseGet(() -> {
                    ReportDTO reportDTO = new ReportDTO(totalPoints, managementDTO.isStopDrug() ? 1 : 0, dailyGoals);
                    return reportService.createReport(memberId, reportDTO);
                });
    }

    private int calculatePoints(ManagementDTO managementDTO) {
        int pointsFromStopDrug = managementDTO.isStopDrug() ? 100 : 0;
        int pointsFromExercise = managementDTO.isExercise() ? 100 : 0;
        int pointsFromMeal = managementDTO.getMeal() >= 2 ? 100 : (managementDTO.getMeal() == 1 ? 50 : 0);
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

    private void updateManagement(Management management, ManagementDTO managementDTO, Report report) {
        Management updatedManagement = management.toBuilder()
                .stopDrug(managementDTO.isStopDrug())
                .exercise(managementDTO.isExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .build();
        managementRepository.save(updatedManagement);
    }
}