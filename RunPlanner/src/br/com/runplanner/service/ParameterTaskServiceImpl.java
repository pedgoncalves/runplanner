package br.com.runplanner.service;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.ParameterTask;

@Service("parameterTask")
public class ParameterTaskServiceImpl extends GenericServiceImpl<ParameterTask, Long> 
	implements ParameterTaskService {

}
