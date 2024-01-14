package com.moneta.hub.moneta.security;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import com.moneta.hub.moneta.model.entity.Role;
import com.moneta.hub.moneta.model.enums.UserStatus;
import com.moneta.hub.moneta.repository.MonetaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCredentialsService implements UserDetailsService {

    private final MonetaUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MonetaUser monetaUser = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User with username does not exist."));
        if (!Objects.equals(monetaUser.getStatus(), UserStatus.ACTIVE)) {
            throw new IllegalArgumentException("User is not verified.");
        }
        return new User(monetaUser.getUsername(), monetaUser.getPassword(), this.mapRolesToAuthorities(monetaUser.getRoles()));
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name())).collect(Collectors.toList());
    }
}
