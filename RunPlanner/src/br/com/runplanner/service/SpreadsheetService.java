package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;

public interface SpreadsheetService extends GenericService<Spreadsheet, Long>{
	
	Spreadsheet removeSchedule(Spreadsheet spreadsheet, Schedule schedule);
	List<Spreadsheet> findByCompetence(Integer month, Integer year, Long adviceId);
	List<Spreadsheet> findByCompetenceOrdered(Integer month, Integer year, Long adviceId);
	List<Spreadsheet> findByCompetenceTeam(Integer month, Integer year, Long adviceId, Long teamId);
	List<Spreadsheet> findByCompetenceAdvice(Integer month, Integer year, Long adviceId);
	List<Spreadsheet> findByStudent(Long studentId);
	List<Spreadsheet> findByStudentInterval(Long studentId, int initialMonth, int finalMonth, int initialYear, int finalYear);	
	List<Spreadsheet> findByStudentDesc(Long studentId);
	Long countByAdvice(Long adviceId);
	Spreadsheet getByStudentDesc(Long studentId);
	Schedule findByStudentActual(Long studentId);
	List<Schedule> findByStudentActualList(Long studentId);
}
