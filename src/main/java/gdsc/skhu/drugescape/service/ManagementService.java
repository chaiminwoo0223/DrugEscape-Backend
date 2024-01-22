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

import java.time.LocalDate;

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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + memberId));
        Report report = processReport(memberId, managementDTO);
        Management management = managementRepository.findByMemberId(memberId)
                .orElseGet(() -> Management.builder().member(member).lastManagedDate(LocalDate.now()).build());
        Management updatedManagement = management.toBuilder()
                .stopDrug(managementDTO.isStopDrug())
                .exercise(managementDTO.isExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .build();
        managementRepository.save(updatedManagement);
    }

    private Report processReport(Long memberId, ManagementDTO managementDTO) {
        int totalPoints = calculatePoints(managementDTO);
        int completedTasks = calculateCompletedTasks(managementDTO);
        int dailyGoals = 25 * completedTasks;
        LocalDate today = LocalDate.now();
        return reportRepository.findByMemberId(memberId)
                .map(existingReport -> updateExistingReport(existingReport, memberId, totalPoints, dailyGoals, today))
                .orElseGet(() -> reportService.createReport(memberId, new ReportDTO(totalPoints, 1, dailyGoals)));
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

    private Report updateExistingReport(Report existingReport, Long memberId, int totalPoints, int dailyGoals, LocalDate today) {
        Management management = managementRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 ID에 대한 관리를 찾을 수 없습니다: " + memberId));
        if (!today.equals(management.getLastManagedDate())) {
            existingReport = existingReport.toBuilder()
                    .maximumDays(existingReport.getMaximumDays() + 1)
                    .point(totalPoints)
                    .dailyGoals(dailyGoals)
                    .build();
            management = management.toBuilder()
                    .lastManagedDate(today)
                    .build();
        } else {
            existingReport = existingReport.toBuilder()
                    .point(totalPoints)
                    .dailyGoals(dailyGoals)
                    .build();
        }
        managementRepository.save(management);
        return reportRepository.save(existingReport);
    }
}