# LinkedIn Enterprise Microservices Ecosystem
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.x-blue.svg)](https://spring.io/projects/spring-cloud)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-326CE5.svg)](https://kubernetes.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%20Mode-231F20.svg)](https://kafka.apache.org/)
[![Neo4j](https://img.shields.io/badge/Neo4j-Graph%20Database-008CC1.svg)](https://neo4j.com/)
[![Redis](https://img.shields.io/badge/Redis-7%20Alpine-DC382D.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg)](https://www.postgresql.org/)

A distributed, event-driven LinkedIn clone backend architected using Spring Boot microservices, polyglot persistence (PostgreSQL + Neo4j + Redis), and asynchronous messaging with Apache Kafka (KRaft). Containerized using Google Jib and orchestrated on Kubernetes with HPA autoscaling, Prometheus/Grafana observability, Zipkin tracing, and integrated Spring AI capabilities.

---

## 1. Architecture Topology & Network Ports
```text
                                  [ Client / Postman / Web ]
                                               │
                                               ▼
               ┌───────────────────────────────────────────────────────────────┐
               │                api-gateway (Port: 9000 -> 80)                 │
               │         Spring Cloud Gateway • Centralized JWT Validation     │
               │         Resilience4j Circuit Breakers • Dynamic Routing       │
               └───────┬───────────────────────┬───────────────────────┬───────┘
                       │                       │                       │
         ┌─────────────┘                       │                       └─────────────┐
         │ HTTP Route                          │ HTTP Route                          │ HTTP Route
         ▼                                     ▼                                     ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐ ┌─────────────────────────────┐
│        user-service         │ │        post-service         │ │     connection-service      │
│         (Port 8080)         │ │         (Port 8000)         │ │         (Port 9030)         │
│  Profiles • Authentication  │ │  Posts • Likes • Comments   │ │ Graph Traversal • Feeds     │
│  Spring Security • JWT      │ │  Spring AI Post Generator   │ │ Redis Cache-Aside Engine    │
└──────────────┬──────────────┘ └──────────────┬──────────────┘ └──────────────┬──────────────┘
               │                               │                               │
     ┌─────────┴─────────┐           ┌─────────┴─────────┐           ┌─────────┴─────────┐
     ▼                   ▼           ▼                   ▼           ▼         ▼         ▼
┌──────────┐       ┌───────────┐┌──────────┐       ┌───────────┐┌─────────┐┌────────┐┌───────────┐
│PostgreSQL│       │   Kafka   ││PostgreSQL│       │   Kafka   ││  Redis  ││ Neo4j  ││   Kafka   │
│ (User DB)│       │ Producer  ││ (Post DB)│       │ Producer  ││ Cache   ││ GraphDB││ Consumer  │
└──────────┘       └─────┬─────┘└──────────┘       └─────┬─────┘└─────────┘└────────┘└─────▲─────┘
                         │                               │                                 │
                         ▼                               ▼                                 │
                 [user_created_topic]            [post_created_topic]                      │
                         │                       [post_liked_topic]                        │
                         │                               │                                 │
                         └───────────────────────────────┴─────────────────────────────────┘
```
**API Gateway (api-gateway):**  Single point of entry on port 9000 (mapped to port 80 in K8s). Performs token extraction, injects user identity headers (X-User-Id), enforces rate limits, and routes to downstream services via Eureka discovery.

**Service Registry (discovery-server):**  Netflix Eureka registry running at http://discovery-server:8761 enabling zero-configuration dynamic routing.

**User Service (user-service):**  Port 8080. Source of truth for credentials and user profiles. Uses PostgreSQL and produces events to Kafka.

**Post Service (post-service):**  Port 8000. Content feed management, post reactions, and LLM-assisted post drafting via Spring AI. Persists to PostgreSQL.

**Connection Service (connection-service):**  Port 9030. Manages graph relations (invitations, 1st/2nd-degree connections, AI vector recommendations) in Neo4j with an in-memory Redis L1 cache.

**Kafka Message Broker:**  2-node KRaft cluster eliminating ZooKeeper. Runs as a StatefulSet handling user_created_topic, post_created_topic, and post_liked_topic.

**Redis:**  In-memory store for 1st-degree connection sets (user:{id}:connections) for sub-millisecond retrieval.

### Network Port Allocation

| Component | Internal Service Port | External / Port-Forward Port | Protocol | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **API Gateway** | `80` | `9000` | HTTP | Client traffic entry & JWT routing |
| **Discovery Server** | `8761` | `8761` | HTTP | Eureka service registry |
| **User Service** | `8080` | `8080` | HTTP | Identity & profile APIs |
| **Post Service** | `8000` | `8000` | HTTP | Feeds, reactions & AI content |
| **Connection Service** | `9030` | `9030` | HTTP | Graph management & network lookups |
| **Neo4j Browser (UI)** | `7474` | `7474` | HTTP | Cypher query console |
| **Neo4j Bolt Driver** | `7687` | `7687` | Bolt | Graph database binary protocol |
| **Kafka Brokers (KRaft)** | `9092` | `9092` | TCP | Inter-pod message transport |
| **Redis Cache** | `6379` | `6379` | RESP | Sub-millisecond connection lookups |
| **Prometheus** | `9090` | `9090` | HTTP | Cluster metrics collection |
| **Grafana** | `80` | `30000` (NodePort) | HTTP | Operational metrics dashboard |
| **Zipkin** | `9411` | `9411` | HTTP | Distributed tracing dashboard |

---
## 2. Technology Stack

| Layer / Category | Technology / Framework | Justification & Architectural Role |
| :--- | :--- | :--- |
| **Language** | **Java 21 (LTS)** | Leverages modern language enhancements, virtual threads, record types, and pattern matching for low-overhead service performance. |
| **Framework** | **Spring Boot 3.2.x** | Enterprise service baseline providing native dependency injection, auto-configuration, Actuator metrics, and HikariCP connection pooling. |
| **Routing & Gateway** | **Spring Cloud Gateway** | Non-blocking (Project Reactor) reverse proxy providing centralized edge authentication, rate-limiting, and header enrichment. |
| **Service Discovery** | **Spring Cloud Netflix Eureka** | Decentralized dynamic service discovery allowing microservices to discover downstream dependencies without hardcoded host addresses. |
| **Relational Database** | **PostgreSQL 16** | Strict relational storage handling tabular transactional workloads (`user-service`, `post-service`) with foreign key constraints and B-Tree indexing. |
| **Graph Database** | **Neo4j 5.x (Cypher Engine)** | Native graph database utilizing index-free adjacency to traverse multi-degree professional connections in constant time relative to localized subgraph density. |
| **In-Memory Cache** | **Redis 7 (Alpine)** | Cache-Aside layer utilizing Redis Sets (`SADD`, `SMEMBERS`, `SINTER`) to handle hot-path connection lookups and mutual connection intersections. |
| **Message Broker** | **Apache Kafka (KRaft Mode)** | Event streaming platform running consensus via KRaft (Kafka Raft Metadata Mode), eliminating ZooKeeper overhead while supporting guaranteed partition ordering. |
| **AI Integration** | **Spring AI (1.0.0-M6)** | Native Java integration with OpenAI LLM endpoints for structured post drafting and high-dimensional semantic vector indexing. |
| **Build & Tooling** | **Maven & Project Lombok** | Declarative dependency management, multi-module configuration, and boilerplate reduction across domain models and DTOs. |

## 3. Component Breakdown & API Contracts

### A. User Service (Identity & Auth)
* **Base Port:** `8080`
* **Datasource:** PostgreSQL (`userdb`)

#### 1. User Registration
* **Route:** `POST /auth/signup`
* **Request JSON:**
```json
{
  "name": "Tarun Chauhan",
  "email": "tarun@example.com",
  "password": "SecurePassword123!"
}
```
* **Response JSON (201 Created):**
```json
{
  "userId": 1,
  "name": "Tarun Chauhan",
  "email": "tarun@example.com",
  "createdAt": "2026-09-05T01:15:30Z"
}
```
