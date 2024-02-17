package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.ManagementDTO;
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
import java.util.stream.Stream;

@Service
public class ManagementService {
    private final ManagementRepository managementRepository;
    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    public ManagementService(ManagementRepository managementRepository,
                             MemberRepository memberRepository,
                             ReportRepository reportRepository) {
        this.managementRepository = managementRepository;
        this.memberRepository = memberRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public void processManagementRecord(Long memberId, ManagementDTO managementDTO) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new EntityNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + memberId));
        Report report = processReport(memberId, managementDTO);
        LocalDate today = LocalDate.now();

        managementRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        existingManagement -> updateManagement(existingManagement, managementDTO, report),
                        () -> createManagement(member, managementDTO, report, today)
                );
    }

    private void createManagement(Member member, ManagementDTO managementDTO, Report report, LocalDate today) {
        Management management = Management.builder()
                .member(member)
                .stopDrug(managementDTO.getStopDrug())
                .exercise(managementDTO.getExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .lastManagedDate(today)
                .build();
        managementRepository.save(management);
    }

    private void updateManagement(Management existingManagement, ManagementDTO managementDTO, Report report) {
        Management updatedManagement = existingManagement.toBuilder()
                .stopDrug(managementDTO.getStopDrug())
                .exercise(managementDTO.getExercise())
                .meal(managementDTO.getMeal())
                .medication(managementDTO.getMedication())
                .report(report)
                .lastManagedDate(LocalDate.now())
                .build();
        managementRepository.save(updatedManagement);
    }


    private Report processReport(Long memberId, ManagementDTO managementDTO) {
        int totalPoints = calculatePoints(managementDTO);
        int dailyGoals = calculateDailyGoals(managementDTO);
        return reportRepository.findByMemberId(memberId)
                .map(existingReport -> updateExistingReport(existingReport, totalPoints, dailyGoals))
                .orElseGet(() -> createNewReport(memberId, totalPoints, dailyGoals));
    }

    private int calculatePoints(ManagementDTO managementDTO) {
        return Stream.of(managementDTO.getStopDrug(), managementDTO.getExercise(), managementDTO.getMeal(), managementDTO.getMedication())
                .mapToInt(value -> value > 0 ? 100 : 0).sum();
    }

    private int calculateDailyGoals(ManagementDTO managementDTO) {
        long completedTasks = Stream.of(managementDTO.getStopDrug(), managementDTO.getExercise(), managementDTO.getMeal(), managementDTO.getMedication())
                .filter(value -> value > 0).count();
        return (int) completedTasks * 25;
    }

    private Report updateExistingReport(Report existingReport, int totalPoints, int dailyGoals) {
        existingReport = existingReport.toBuilder()
                .point(existingReport.getPoint() + totalPoints) // 포인트 누적
                .maximumDays(existingReport.getMaximumDays() + 1) // 단약일 누적
                .dailyGoals(dailyGoals)
                .build();
        return reportRepository.save(existingReport);
    }

    private Report createNewReport(Long memberId, int totalPoints, int dailyGoals) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new EntityNotFoundException("Member not found for ID: " + memberId));
        Report newReport = Report.builder()
                .member(member)
                .point(totalPoints)
                .maximumDays(1)
                .dailyGoals(dailyGoals)
                .build();
        return reportRepository.save(newReport);
    }
}