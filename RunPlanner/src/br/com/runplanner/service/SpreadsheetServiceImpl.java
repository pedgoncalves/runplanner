package br.com.runplanner.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Schedule;
import br.com.runplanner.domain.Spreadsheet;
import br.com.runplanner.exception.EntityNotFoundException;

@Service("spreadsheetService")
public class SpreadsheetServiceImpl extends GenericServiceImpl<Spreadsheet, Long> implements SpreadsheetService {

	@Autowired
	private ScheduleService scheduleService;

    @Transactional
	public Spreadsheet removeSchedule(Spreadsheet spreadsheet, Schedule schedule) {
		spreadsheet = this.loadById(spreadsheet.getId());
		spreadsheet.getSchedules().remove(schedule);
		
    	try {
			scheduleService.deleteById(schedule.getId());
	        this.update(spreadsheet);
		} catch (EntityNotFoundException e) {
			// Ja foi removido antes
		}    
        
        return spreadsheet;
	}
	
	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByCompetence(Integer month, Integer year, Long adviceId) {
		
		Query query = entityManager.createNamedQuery("Spreadsheet.findByCompetence");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		return (List<Spreadsheet>)query.getResultList();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByCompetenceOrdered(Integer month, Integer year, Long adviceId) {
		
		Query query = entityManager.createNamedQuery("Spreadsheet.findByCompetenceOrdered");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		return (List<Spreadsheet>)query.getResultList();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByCompetenceTeam(Integer month, Integer year, Long adviceId, Long teamId) {
		
		Query query = entityManager.createNamedQuery("Spreadsheet.findByCompetenceTeam");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		query.setParameter("teamId", teamId);
		return (List<Spreadsheet>)query.getResultList();
		
	}

	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByCompetenceAdvice(Integer month, Integer year, Long adviceId) {
		Query query = entityManager.createNamedQuery("Spreadsheet.findByCompetenceAdvice");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("adviceId", adviceId);
		return (List<Spreadsheet>)query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByStudent(Long studentId) {
		Query query = entityManager.createNamedQuery("Spreadsheet.findByStudent");
		query.setParameter("studentId", studentId);
		return (List<Spreadsheet>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByStudentDesc(Long studentId) {
		Query query = entityManager.createNamedQuery("Spreadsheet.findByStudentDesc");
		query.setParameter("studentId", studentId);
		return (List<Spreadsheet>)query.getResultList();
	}
		
	public Spreadsheet getByStudentDesc(Long studentId) {
		Query query = entityManager.createNamedQuery("Spreadsheet.findByStudentDesc");
		query.setParameter("studentId", studentId);
		
		
		try {
			return (Spreadsheet) query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	}

	@SuppressWarnings("unchecked")
	public List<Spreadsheet> findByStudentInterval(Long studentId,
			int initialMonth, int finalMonth, int initialYear, int finalYear) {
		Query query = entityManager.createNamedQuery("Spreadsheet.findByStudentInterval");
		query.setParameter("studentId", studentId);
		query.setParameter("initialMonth", initialMonth);
		query.setParameter("finalMonth", finalMonth);
		query.setParameter("initialYear", initialYear);
		query.setParameter("finalYear", finalYear);
		return (List<Spreadsheet>)query.getResultList();
	}
	
	public Schedule findByStudentActual(Long studentId) {
		Calendar calendar = GregorianCalendar.getInstance( TimeZone.getTimeZone("America/Sao_Paulo") );
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		
		String sql = "select sc.* from Spreadsheet sp, Spreadsheet_Schedule spc, Schedule sc " +
				"where sp.id = spc.Spreadsheet_id and sc.id = spc.schedules_id " +
				"and sp.student_id= :studentId " +
				"and sc.date >= DATE(:date) " +
				"order by sc.date asc";
		
		
		Query query = entityManager.createNativeQuery(sql,Schedule.class);
		query.setParameter("studentId", studentId);
		query.setParameter("date", calendar.getTime());
		query.setMaxResults(1);

		try {
			return (Schedule) query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	}
		
	public Long countByAdvice(Long adviceId) {
		
		Query query = entityManager.createNamedQuery("Spreadsheet.countByAdvice");		
		query.setParameter("adviceId", adviceId);
		
		Long count = (Long) query.getSingleResult();
		
		if ( count == null ) {
			count = 0l;
		}
		return count;
		
	}

}
