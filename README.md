# DataShare — Backend

API REST Spring Boot pour la plateforme de transfert de fichiers DataShare.

---

## 🛠️ Stack technique

| Technologie     | Version| Rôle                      |
|-----------------|--------|---------------------------|
| Java            | 21     | Langage                   |
| Spring Boot     | 3.5.x  | Framework                 |
| Spring Security | 6.5.x  | Authentification          |
| PostgreSQL      | 15     | Base de données           |
| Docker          | 24.x   | Conteneurisation BDD      |
| JWT             | HS256  | Tokens d'authentification |
| MapStruct       | 1.5.5  | Mapping DTO/Entité        |
| Lombok          | 1.18.x | Réduction boilerplate     |
| JaCoCo          | 0.8.11 | Couverture de code        |
| k6              | 2.0.0  | Tests de performance      |

---

## 📋 Prérequis

- Java 21
- Maven 3.9.x
- Docker et Docker Compose
- Git

---

## ⚙️ Installation

### 1. Cloner le repository

```bash
git clone git@github.com:gmt86/DevOps-Projet_3-backend---Pilotez_le_developpement_d_une_solution_informatique.git
cd backend <-- dossier projet
```

### 2. Configurer les variables d'environnement

Ses variables se trouvent dans le fichier `.env` et sont modifiables


### 3. Démarrer la base de données

```bash
docker compose up -d
```

Vérifier que le conteneur tourne :

```bash
docker ps
```

### 4. Lancer le backend

```bash
./mvnw spring-boot:run
```

➡️ API disponible sur `http://localhost:8080`

---

## 🔗 Endpoints API

Documentation complète disponible sur SwaggerHub :
👉 https://app.swaggerhub.com/apis/etudiant-aad/DataShare/1.0.0

| Méthode | Endpoint                         | Description         | Auth |
|---------|----------------------------------|---------------------|------|
| POST    | `/api/auth/register`             | Inscription         | ❌   |
| POST    | `/api/auth/login`                | Connexion           | ❌   |
| POST    | `/api/fichiers`                  | Upload fichier      | ✅   |
| GET     | `/api/fichiers`                  | Historique fichiers | ✅   |
| GET     | `/api/fichiers/{token}`          | Métadonnées fichier | ❌   |
| POST    | `/api/fichiers/{token}/download` | Télécharger fichier | ❌   |
| DELETE  | `/api/fichiers/{id}`             | Supprimer fichier   | ✅   |

---

## 🧪 Tests

### Lancer les tests unitaires

```bash
# Démarrer la base de données d'abord
docker compose up -d

./mvnw test
```

### Rapport de couverture JaCoCo

```bash
./mvnw test
# Rapport disponible dans : target/site/jacoco/index.html
```

Couverture actuelle : **71%** ✅

### Tests de performance (k6)

```bash
# Backend doit être démarré
k6 run k6/upload-test.js
```

---

## 📁 Structure du projet
![Structure backend](https://raw.githubusercontent.com/gmt86/DevOps-Projet_3-docs---Pilotez_le_developpement_d_une_solution_informatique/main/screenshots/backend-structure-1.png)
![Structure backend](https://raw.githubusercontent.com/gmt86/DevOps-Projet_3-docs---Pilotez_le_developpement_d_une_solution_informatique/main/screenshots/backend-structure-2.png)


---

## 🔒 Sécurité

- Authentification JWT (HS256)
- Mots de passe hashés avec BCrypt
- CORS configuré pour `http://localhost:4200`
- Validation des entrées avec Bean Validation
- Types de fichiers interdits configurables
- Taille maximale des fichiers configurable

---

## 📊 Performance

Test k6 sur l'endpoint upload avec 10 utilisateurs simultanés :

| Métrique      | Résultat | Seuil       |
|---------------|----------|-------------|
| p(95) durée   | 354ms    | < 2000ms ✅ |
| Taux d'échec  | 0.20%    | < 10% ✅    |
| Durée moyenne | 142ms    | -           |
