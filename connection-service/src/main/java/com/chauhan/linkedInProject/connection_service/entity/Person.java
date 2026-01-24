package com.chauhan.linkedInProject.connection_service.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node
@Data
@Builder
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    @Property("userId")
    private Long userId;

    private String name;
}
