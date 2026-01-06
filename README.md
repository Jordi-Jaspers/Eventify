<div align="center">

# Eventify

**An intuitive tool to manage and monitor your services via intelligent event creation**

[![GitHub stars](https://img.shields.io/github/stars/JFrameOSS/JFrame?style=social)](https://github.com/JFrameOSS/JFrame/)
[![CICD](https://github.com/Jordi-Jaspers/Eventify/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/Jordi-Jaspers/Eventify/actions/workflows/ci.yml)

[![License](https://img.shields.io/github/license/Jordi-Jaspers/Eventify)](https://www.gnu.org/licenses/gpl-3.0.html#license-text)
[![Issues](https://img.shields.io/github/issues/Jordi-Jaspers/Eventify?color=red)](https://github.com/Jordi-Jaspers/Eventify/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/Jordi-Jaspers/Eventify?color=green)](https://github.com/Jordi-Jaspers/Eventify/pulls)
[![Commit Activity](https://img.shields.io/github/commit-activity/m/Jordi-Jaspers/Eventify)](https://github.com/Jordi-Jaspers/Eventify/graphs/commit-activity)
[![Last Commit](https://img.shields.io/github/last-commit/Jordi-Jaspers/Eventify)](https://github.com/Jordi-Jaspers/Eventify/commits/master)


[Introduction](#-introduction) •
[Quick Start](#-quick-start) •
[Stack](#-stack) •
[Contributing](#-contributing) •
[License](#-license)

</div>

---

## 📖 Introduction

**Eventify** is a modern platform designed to simplify service monitoring and management. By leveraging intelligent event creation, it allows teams to track critical occurrences, manage organizations, and visualize service health in real-time. Whether you are debugging a microservice architecture or monitoring a monolithic application, Eventify provides the insights you need.

## 🚀 Quick Start

Get Eventify running locally in minutes.

### Prerequisites
- [Docker](https://www.docker.com/) (for database and message broker)
- [Java 25](https://adoptium.net/) (for the backend)
- [Bun](https://bun.sh/) or Node.js (for the frontend)

### Installation

1. **Start the Infrastructure**
   Launch the required services (TimescaleDB, Inbucket).
   ```bash
   docker-compose up -d
   ```

2. **Run the Backend**
   Start the Spring Boot server.
   ```bash
   cd server
   ./gradlew bootRun
   ```

3. **Run the Frontend**
   Install dependencies and start the SvelteKit development server.
   ```bash
   cd client
   bun install
   bun run dev
   ```

   The application will be available at [http://localhost:5173](http://localhost:5173).

## 🛠️ Stack

**Backend**
- [Spring Boot](https://spring.io/projects/spring-boot) - Core application framework
- [TimescaleDB](https://www.timescale.com/) - Time-series database
- [Liquibase](https://www.liquibase.org/) - Database migration

**Frontend**
- [SvelteKit](https://kit.svelte.dev/) - Web framework
- [TailwindCSS](https://tailwindcss.com/) - Styling
- [Shadcn Svelte](https://www.shadcn-svelte.com/) - UI Components
- [Lucide](https://lucide.dev/) - Icons

## 🤝 Contributing

We welcome contributions! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to submit pull requests, report issues, or suggest improvements.

## 📜 License

Licensed under [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html#license-text).

---

<div align="center">
    <b>Author:</b> Jordi Jaspers
    <br>
    <a href="https://github.com/Jordi-Jaspers">
        <img alt="GitHub" src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white"/>
    </a>
    <a href="https://ie.linkedin.com/in/jordi-jaspers">
        <img alt="LinkedIn" src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"/>
    </a>
    <a href="https://buymeacoffee.com/jaspers">
        <img alt="Buy me a coffee" src="https://img.shields.io/badge/Donate-Buy%20me%20a%20coffee-FF813F?style=for-the-badge&logo=buy-me-a-coffee&logoColor=white"/>
    </a>
</div>
