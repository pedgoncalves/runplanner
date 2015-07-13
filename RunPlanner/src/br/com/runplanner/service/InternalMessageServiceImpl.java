package br.com.runplanner.service;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.InternalMessage;

@Service("internalMessageService")
public class InternalMessageServiceImpl extends
		GenericServiceImpl<InternalMessage, Long> implements
		InternalMessageService {

}
