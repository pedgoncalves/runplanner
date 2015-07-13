package br.com.runplanner.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.Pessoa;

@SuppressWarnings("deprecation")
@Service("userDetailsService") 
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired 
	private PessoaService pessoaService;

	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		List<Pessoa> userEntity = pessoaService.loadByEmail(username);
		if (userEntity == null || userEntity.size() == 0) {
			throw new UsernameNotFoundException("user not found");
		}
		
		int qtdAtivo = 0;
		Pessoa ativo = null;
		for(Pessoa p: userEntity) {
			if(p.getActive()) {
				qtdAtivo++;
				ativo = p;
			}
		}
		if(qtdAtivo == 0) {
			throw new LockedException("usuário inativo");
		} else if(qtdAtivo>1) {
			throw new AuthenticationServiceException("Cadastro Duplicado");
		}

		return buildUserFromUserEntity(ativo);
	}

	private User buildUserFromUserEntity(Pessoa userEntity) {

		String username = userEntity.getEmail();
		String password = userEntity.getPassword();
		boolean enabled = userEntity.isActive();
		boolean accountNonExpired = userEntity.isActive();
		boolean credentialsNonExpired = userEntity.isActive();
		boolean accountNonLocked = userEntity.isActive();

		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new GrantedAuthorityImpl(userEntity.getRole().getDescription()));

		return new User(username, password, enabled, accountNonExpired, 
				credentialsNonExpired, accountNonLocked, authorities);
	}


}
