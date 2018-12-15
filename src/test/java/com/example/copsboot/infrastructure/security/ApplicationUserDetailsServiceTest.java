package com.example.copsboot.infrastructure.security;


import com.example.copsboot.infrastructure.security.config.ApplicationUserDetails;
import com.example.copsboot.infrastructure.security.config.ApplicationUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import com.example.copsboot.user.UserRepository;
import com.example.copsboot.user.Users;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationUserDetailsServiceTest {


    @Test
    public void givenExistingUsername_whenLoadingUser_userIsReturned() {
        UserRepository repository = mock(UserRepository.class);
        ApplicationUserDetailsService service = new ApplicationUserDetailsService
                (repository);
        when(repository.findByEmailIgnoreCase(Users.OFFICER_EMAIL))
                .thenReturn(Optional
                        .of(Users.officer()));
        UserDetails userDetails = service.loadUserByUsername(Users.OFFICER_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(Users.OFFICER_EMAIL);
        assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority).contains("ROLE_OFFICER");
        assertThat(userDetails).isInstanceOfSatisfying(ApplicationUserDetails.class, applicationUserDetails -> {
                    assertThat(applicationUserDetails.getUserId())
                            .isEqualTo(Users.officer().getId());
                });
    }

    @Test
    public void givenNotExistingUsername_whenLoadingUser_exceptionThrown() {
        UserRepository repository = mock(UserRepository.class);
        ApplicationUserDetailsService service = new ApplicationUserDetailsService
                (repository);
        when(repository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("i@donotexist.com"))
                        .isInstanceOf(UsernameNotFoundException.class);
    }
}
