package com.ahd.backend.carcontracts.appuser.services;

import com.ahd.backend.carcontracts.appuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository users;


    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user"));
    }

}
