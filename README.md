# Task Management API

**Task Management API** est une API REST backend professionnelle de gestion de tâches développée avec **Java et Spring Boot**.

Le projet est conçu comme une plateforme backend évolutive mettant l'accent sur la **sécurité, la qualité du code, la stratégie de tests, l'architecture, les performances et les pratiques DevOps**.

L'objectif n'est pas simplement de réaliser un CRUD, mais de construire progressivement une application backend proche des standards utilisés dans des environnements professionnels.

---

## Vision du projet

Task Management API a pour objectif de fournir une plateforme permettant à des utilisateurs et des équipes de :

* gérer leurs tâches ;
* organiser les priorités et les statuts ;
* attribuer des tâches ;
* gérer les utilisateurs et leurs rôles ;
* contrôler les permissions ;
* suivre les modifications ;
* conserver un historique des actions ;
* sécuriser les accès à l'API ;
* exploiter une API documentée et testée.

L'architecture est pensée pour pouvoir évoluer progressivement vers une application complète de gestion de travail collaboratif.

---

# Fonctionnalités

## Authentification et sécurité

* Authentification utilisateur
* Authentification JWT
* Gestion des rôles
* Autorisation des endpoints
* Filtre d'authentification JWT
* Gestion des tokens révoqués
* Hashage sécurisé des mots de passe
* Réinitialisation du mot de passe
* Validation des données entrantes
* Gestion centralisée des erreurs
* Protection des ressources selon les droits de l'utilisateur

---

## Gestion des utilisateurs

Le système permet de gérer les utilisateurs et leurs informations.

Fonctionnalités prévues :

* création d'un compte ;
* authentification ;
* consultation du profil ;
* modification des informations ;
* changement du mot de passe ;
* réinitialisation du mot de passe ;
* gestion des rôles ;
* administration des utilisateurs.

### Rôles

```text
USER
ADMIN
```

Le modèle d'autorisation sera progressivement enrichi afin de permettre une gestion plus fine des permissions.

---

# Gestion des tâches

Une tâche contient notamment :

* titre ;
* description ;
* statut ;
* priorité ;
* date de création ;
* date de modification.

### Statuts

Le domaine métier utilise des types dédiés pour représenter les différents états d'une tâche.

### Priorités

Les priorités sont également représentées par un type dédié afin de garantir la cohérence des données.

Les fonctionnalités futures incluent :

* assignation ;
* échéances ;
* catégories ;
* tags ;
* commentaires ;
* historique ;
* recherche ;
* filtrage ;
* tri ;
* pagination.

---


### Controller

Responsable de l'exposition des endpoints REST et de la gestion des requêtes HTTP.

### Service

Contient la logique métier de l'application.

### Repository

Responsable de l'accès aux données avec Spring Data JPA.

### Entity

Représente les modèles persistés en base de données.

### DTO

Permet de contrôler les données entrantes et sortantes de l'API sans exposer directement les entités internes.

### Exception

Centralise la gestion des erreurs applicatives.

### Filter

Intercepte notamment les requêtes afin de gérer l'authentification JWT.

---

# Stack technique

| Technologie         | Utilisation              |
| ------------------- | ------------------------ |
| **Java 21**         | Langage principal        |
| **Spring Boot**     | Framework backend        |
| **Spring Security** | Sécurité et autorisation |
| **Spring Data JPA** | Persistance              |
| **Hibernate**       | ORM                      |
| **PostgreSQL**      | Base de données          |
| **JWT**             | Authentification         |
| **Maven**           | Build et dépendances     |
| **JUnit**           | Tests                    |
| **Git / GitHub**    | Versionnement            |

Des technologies complémentaires seront intégrées progressivement lorsque leur utilisation répondra à un besoin architectural ou fonctionnel réel.

---

# Architecture de sécurité

Le système de sécurité repose sur une authentification basée sur JWT.

```text
Client
   │
   │ Login
   ▼
AuthController
   │
   ▼
Authentication
   │
   ▼
JWT
   │
   ▼
Client
   │
   │ Authorization: Bearer <token>
   ▼
JWT Authentication Filter
   │
   ▼
Spring Security
   │
   ├── USER
   │
   └── ADMIN
   │
   ▼
Protected API
```

La sécurité sera progressivement renforcée avec :

* Access Token / Refresh Token ;
* rotation des tokens ;
* révocation ;
* permissions granulaires ;
* limitation des tentatives ;
* rate limiting ;
* protection des endpoints sensibles ;
* tests de sécurité.

---

# Stratégie de tests

Les tests constituent une partie centrale du projet.

L'objectif est de construire une véritable **stratégie de tests**, couvrant plusieurs niveaux.

```text
                Tests E2E
                   ▲
                   │
             Tests API
                   ▲
                   │
          Tests d'intégration
                   ▲
                   │
             Tests unitaires
```

## Tests unitaires

Tester indépendamment :

* services ;
* logique métier ;
* règles métier ;
* validation ;
* composants isolés.

## Tests d'intégration

Tester l'interaction entre :

* Spring Boot ;
* services ;
* repositories ;
* PostgreSQL ;
* sécurité.

## Tests API

Tester les endpoints REST :

* codes HTTP ;
* payloads ;
* validations ;
* erreurs ;
* authentification ;
* autorisation.

## Tests de sécurité

Vérifier notamment :

* authentification ;
* JWT ;
* expiration ;
* révocation ;
* rôles ;
* permissions ;
* accès non autorisés.

## Outils prévus

* JUnit
* Spring Boot Test
* Mockito
* MockMvc
* Testcontainers
* JaCoCo

La couverture de code ne sera pas considérée comme l'unique indicateur de qualité : la pertinence des scénarios et la couverture des risques métier resteront prioritaires.

---

# Gestion des erreurs

L'application utilise une gestion centralisée des exceptions avec `@RestControllerAdvice`.

Les erreurs suivent un format cohérent :

```json
{
  "timestamp": "2026-10-05T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "La tâche demandée n'existe pas",
  "path": "/tasks/10"
}
```

L'objectif est de fournir aux clients de l'API des réponses prévisibles et exploitables.

---

# Base de données

Le projet utilise **PostgreSQL** avec **Spring Data JPA / Hibernate**.

La gestion de la base évoluera progressivement vers :

* migrations versionnées ;
* contraintes d'intégrité ;
* indexation ;
* optimisation des requêtes ;
* analyse des performances ;
* prévention des problèmes N+1 ;
* stratégie de transaction.

---

# API REST

Les principaux domaines fonctionnels sont :

```text
/api/auth
/api/tasks
/api/users
/api/admin
```

Les endpoints seront progressivement documentés avec **OpenAPI / Swagger**.

Exemple de ressources :

```text
POST   /api/auth/login
POST   /api/auth/register

GET    /api/tasks
POST   /api/tasks
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
DELETE /api/tasks/{id}

GET    /api/users
```

La liste exacte des endpoints évoluera avec le modèle fonctionnel.

---

# Docker

La plateforme sera progressivement conteneurisée afin de permettre une exécution reproductible de l'environnement :

```text
┌─────────────────────┐
│   Spring Boot API   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│     PostgreSQL      │
└─────────────────────┘
```

L'environnement Docker pourra ensuite intégrer des services complémentaires lorsque cela sera justifié.

---

# CI/CD

Le projet intégrera une pipeline **GitHub Actions** permettant d'automatiser :

```text
Push / Pull Request
        │
        ▼
     Build
        │
        ▼
   Unit Tests
        │
        ▼
Integration Tests
        │
        ▼
 Quality Checks
        │
        ▼
 Security Checks
        │
        ▼
     Package
```

L'objectif est de garantir qu'une modification ne puisse être intégrée sans validation automatisée.

---

# Observabilité

L'application évoluera vers une architecture observable comprenant :

* Spring Actuator ;
* health checks ;
* logs structurés ;
* métriques ;
* monitoring ;
* traçabilité des requêtes ;
* correlation ID.

L'objectif est de pouvoir diagnostiquer efficacement les problèmes en environnement réel.

---

# Performance

Les performances seront progressivement étudiées sur plusieurs niveaux :

### Base de données

* index ;
* requêtes SQL ;
* pagination ;
* plans d'exécution.

### JPA / Hibernate

* prévention du N+1 ;
* stratégies de chargement ;
* transactions ;
* projections.

### Application

* cache ;
* traitement efficace ;
* limitation des ressources ;
* profiling.

### Infrastructure

* Docker ;
* Redis lorsque nécessaire ;
* monitoring.

Les optimisations seront réalisées à partir de mesures et non de suppositions.

---

# Qualité du code

Le projet suit progressivement plusieurs principes :

* SOLID ;
* Clean Code ;
* séparation des responsabilités ;
* DRY ;
* KISS ;
* principe de moindre privilège ;
* validation des entrées ;
* gestion explicite des erreurs ;
* code testable ;
* documentation des décisions techniques.

Les choix d'architecture importants seront documentés afin de rendre le raisonnement technique compréhensible.

---

# Décisions d'architecture

Le projet pourra utiliser des **Architecture Decision Records (ADR)** pour documenter les décisions importantes.

Exemples :

```text
ADR-001 — Choix de Spring Boot
ADR-002 — Choix de PostgreSQL
ADR-003 — Authentification JWT
ADR-004 — Stratégie de gestion des erreurs
ADR-005 — Stratégie de tests
ADR-006 — Utilisation de Docker
```

L'objectif est de montrer non seulement **ce qui a été développé**, mais également **pourquoi les choix techniques ont été faits**.

---

# Installation

## Prérequis

* Java 21+
* PostgreSQL
* Maven
* Git

## Cloner le projet

```bash
git clone git@github-pro:Brejnev-fullstack/task-management-api.git

cd task-management-api
```

## Variables d'environnement

Créer un fichier `.env` local contenant les valeurs adaptées à votre environnement :

```env
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret
```

## Lancer l'application

### Windows

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

L'API est disponible par défaut sur :

```text
http://localhost:8081
```

---

# Exécuter les tests

### Windows

```bash
mvnw.cmd test
```

### Linux / macOS

```bash
./mvnw test
```

---

# Roadmap

## Phase 1 — Fondations

* [x] API REST
* [x] Gestion des tâches
* [x] Gestion des utilisateurs
* [x] Authentification
* [x] JWT
* [x] Gestion des rôles
* [x] Validation
* [x] Gestion centralisée des exceptions

## Phase 2 — API avancée

* [ ] Pagination
* [ ] Tri
* [ ] Filtrage
* [ ] Recherche
* [ ] Assignation des tâches
* [ ] Échéances
* [ ] Commentaires
* [ ] Historique
* [ ] Audit logs

## Phase 3 — Sécurité avancée

* [ ] Refresh Tokens
* [ ] Rotation des tokens
* [ ] Permissions granulaires
* [ ] Rate limiting
* [ ] Renforcement JWT
* [ ] Tests de sécurité

## Phase 4 — Stratégie de tests

* [ ] Tests unitaires complets
* [ ] Tests d'intégration
* [ ] Tests API
* [ ] Tests de sécurité
* [ ] Testcontainers
* [ ] JaCoCo
* [ ] Tests de régression

## Phase 5 — Architecture et qualité

* [ ] Refactoring architectural
* [ ] ADR
* [ ] Documentation technique
* [ ] Analyse de qualité
* [ ] SonarQube / SonarCloud

## Phase 6 — DevOps

* [ ] Docker
* [ ] Docker Compose
* [ ] GitHub Actions
* [ ] CI/CD
* [ ] Gestion des environnements

## Phase 7 — Observabilité et performance

* [ ] Spring Actuator
* [ ] Logs structurés
* [ ] Métriques
* [ ] Monitoring
* [ ] Optimisation PostgreSQL
* [ ] Optimisation JPA
* [ ] Redis / Cache

## Phase 8 — Production

* [ ] Documentation OpenAPI complète
* [ ] Configuration sécurisée des environnements
* [ ] Déploiement
* [ ] Monitoring production
* [ ] Stratégie de sauvegarde
* [ ] Documentation d'exploitation

---

# Documentation

La documentation du projet sera progressivement organisée autour de :

```text
docs/
├── architecture/
├── api/
├── security/
├── testing/
├── database/
├── deployment/
└── adr/
```

Elle permettra de documenter les choix techniques, les procédures et les évolutions importantes du projet.

---

# Objectif professionnel

Ce projet constitue un laboratoire pratique permettant de démontrer des compétences en :

* Java ;
* Spring Boot ;
* Spring Security ;
* conception d'API REST ;
* architecture backend ;
* PostgreSQL ;
* JPA / Hibernate ;
* JWT ;
* tests logiciels ;
* stratégie de tests ;
* Docker ;
* CI/CD ;
* observabilité ;
* optimisation ;
* qualité logicielle.

L'objectif final est de transformer progressivement cette API en un **projet backend professionnel complet**, démontrant la capacité à concevoir, sécuriser, tester, documenter, déployer et maintenir une application moderne.

---

# Auteur

**Brejnev Ngondi**

Développeur Full Stack — Java / Spring Boot / JavaScript / TypeScript

---

# Licence

Ce projet est distribué sous licence MIT.
