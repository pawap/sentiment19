package sentiments.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sentiments.config.persistence.model.BackendUserPrincipal;
import sentiments.config.persistence.model.User;
import sentiments.config.persistence.repository.UserRepository;

/**
 * @author Paw
 */
@Service
public class BackendUserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new BackendUserPrincipal(user);
    }
}