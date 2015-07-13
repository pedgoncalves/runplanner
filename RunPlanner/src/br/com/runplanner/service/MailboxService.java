package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.Mailbox;
import br.com.runplanner.domain.MailboxGroup;

public interface MailboxService extends GenericService<Mailbox, Long> {
	List<Mailbox> findByUserId(Long userId);
	List<MailboxGroup> findByUserIdGrouped(Long userId);
	Long getNewByUserId(Long userId);
}
