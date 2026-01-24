package com.chauhan.linkedInProject.connection_service.service;

import com.chauhan.linkedInProject.connection_service.entity.Person;
import com.chauhan.linkedInProject.connection_service.exception.BadRequestException;
import com.chauhan.linkedInProject.connection_service.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public void createPerson(Long userId, String name) {
        boolean alreadyExistUserId = personRepository.existsByUserId(userId);

        if (alreadyExistUserId) {
            throw new BadRequestException("You can not create the User, User already exists with userId {}"+userId);
        }
        Person person = Person.builder().name(name).userId(userId).build();
        personRepository.save(person);
    }

}
