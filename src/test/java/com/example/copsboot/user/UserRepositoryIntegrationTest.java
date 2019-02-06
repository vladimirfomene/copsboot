package com.example.copsboot.user;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;


@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(SpringProfiles.INTEGRATION_TEST)
public class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository repository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void testSaveUser() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.OFFICER);

        User user = repository.save(new User(repository.nextId(),
                "alex.foley@beverly-hills.com",
                "my-secret-pwd",
                roles));
        assertThat(user).isNotNull();
        assertThat(repository.count()).isEqualTo(1L);
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM copsboot_user",
                Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM user_roles",
                Long.class)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject("SELECT roles FROM user_roles",
                String.class)).isEqualTo("OFFICER");
    }
}
