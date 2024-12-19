<p align="center">
    <h1 align="center">Eventify</h1>
</p>
<p align="center">Eventify.io - An intuitive tool to manage and monitor your services via intelligent event creation.</p>
<p align="center">
    <img alt="license" src="https://img.shields.io/github/license/Jordi-Jaspers/Eventify"> 
    <img alt="GitHub release (with filter)" src="https://img.shields.io/github/v/release/Jordi-Jaspers/Eventify?sort=semver">
    <img alt="GitHub Issues or Pull Requests" src="https://img.shields.io/github/issues/Jordi-Jaspers/Eventify?color=red">
    <img src="https://img.shields.io/github/commit-activity/m/Jordi-Jaspers/Eventify" alt="Commit Activity" >
    <img src="https://img.shields.io/github/last-commit/Jordi-Jaspers/Eventify" alt="Last Commit" >
</p>

---

**Author:** Jordi Jaspers [[Github](https://github.com/Jordi-Jaspers "Github Page"), [Linkedin](https://www.linkedin.com/in/jordi-jaspers/ "Linkedin Page")]
<p align="left">
<a href="https://ie.linkedin.com/in/jordi-jaspers">
 <img alt="Mail" title="Connect via email" src="https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white"/>
</a>
<a href="https://ie.linkedin.com/in/jordi-jaspers">
 <img alt="LinkedIn" title="Connect on LinkedIn" src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white"/>
</a>
<a href="https://github.com/Jordi-Jaspers?tab=followers">
 <img alt="followers" title="Follow me on Github" src="https://custom-icon-badges.demolab.com/github/followers/Jordi-Jaspers?color=236ad3&labelColor=1155ba&style=for-the-badge&logo=person-add&label=Follow&logoColor=white"/>
</a>
<a href="https://github.com/Jordi-Jaspers?tab=repositories&sort=stargazers">
 <img alt="total stars" title="Total stars on GitHub" src="https://custom-icon-badges.demolab.com/github/stars/Jordi-Jaspers?color=55960c&style=for-the-badge&labelColor=488207&logo=star"/>
</a>
<a href="https://buymeacoffee.com/jaspers">
    <img alt="Donate" title="Donate" src="https://img.shields.io/badge/Donate-Buy%20me%20a%20coffee-FF813F?style=for-the-badge&logo=buy-me-a-coffee&logoColor=white"/>
</a>
</p>

---

## 📝 Introduction

A high-performance, scalable monitoring solution designed to track and visualize the health and status of distributed systems in real-time. This application provides a robust platform for collecting, processing, and displaying monitoring events from multiple external systems through a user-friendly dashboard interface.

**Website Preview:** https://eventify.io/  
**Website Health endpoint:** https://eventify.io/actuator/health  
**Server API description:** https://eventify.io/api/public/docs/openapi.html

### Overview  
The Monitoring Service is built to handle high-throughput event processing with real-time updates, making it ideal for organizations needing to monitor critical infrastructure and services. It features:

- **Real-time Monitoring**: Instant visibility into system health through WebSocket-powered live updates
- **Scalable Architecture**: Horizontally scalable design using event-driven microservices
- **Efficient Event Processing**: Smart batching and processing of events to handle high-volume monitoring data
- **Team-based Access Control**: Organized access management with team-based dashboard sharing
- **Customizable Dashboards**: Flexible dashboard configuration with support for grouped and ungrouped checks
- **High Availability**: Built on a reliable PostgreSQL cluster with replication for data durability
- **External System Integration**: Simple REST API for external systems to report their status

Whether you're monitoring a handful of services or thousands of endpoints, this platform is designed to provide reliable, real-time insights into your systems' operational status.

### Concept 1: Websocket / Scaling
![concept_1.png](documentation/assets/concept_1.png)

### Concept 2: Event Processing
![concept_2.png](documentation/assets/concept_2.png)

### Excalidraw Diagram
https://excalidraw.com/#json=spCc5QGt8tOYZ-c9SCpLn,dXvLww8Lo9tZxBbOlpO_NA

### Database Schema 📊
https://dbdiagram.io/d/Eventify-673f8ef8e9daa85aca4c0697

## Dang, you found a bug, now what? 🐞

No worries, just check out our [**issues**](https://github.com/Jordi-Jaspers/Eventify) section on GitHub. If you can't find your issue, feel free to create a new one. We trample that bug as soon as possible.

## ☕️ Support & Contributions

#### Want to contribute to the project?
All the extra help is welcome! Just fork the project and create a pull request. If you have any questions, feel free to contact me via email <jordijaspers@gmail.com> or discord for any additional information.

#### Any more ideas for features?
Feel free to create an issue and we can discuss it further.

#### Want to support the project?
You can support and keep alive your anime sanctum by donating here:

<p>
<a href="https://buymeacoffee.com/jaspers">
    <img alt="Donate" title="Donate" src="https://img.shields.io/badge/Donate-Buy%20me%20a%20coffee-FF813F?style=for-the-badge&logo=buy-me-a-coffee&logoColor=white"/>
</p>

## ⭐ Stargazers over time
[![Stargazers over time](https://starchart.cc/Jordi-Jaspers/Eventify.svg?variant=adaptive)](https://starchart.cc/Jordi-Jaspers/Eventify)

## 🙌 Credits

For now nobody really... 🤷‍♂️

## 🛠️ Stack

- [Spring Boot](https://spring.io/projects/spring-boot) - Java framework for building back-end applications.
- [Redis](https://redis.io/) - In-memory data structure store.
- [SvelteKit](https://kit.svelte.dev/) - Frontend framework for building web applications.
- [TailwindCSS](https://tailwindcss.com/) - Utility-first CSS framework.
- [Shadcn Svelte](https://www.shadcn-svelte.com/) - Svelte components library.
- [Lucide](https://lucide.dev/) - SVG icons library.
- [Cloudflare](https://www.cloudflare.com/) - CDN and DNS provider.
- [Traefik](https://traefik.io/) - Reverse proxy and load balancer.
- [Docker](https://www.docker.com/) - Containerization platform.
- [Liquibase](https://www.liquibase.org/) - Database migration tool
- [TimescaleDB](https://www.timescale.com/) - Time-series database
- [Git](https://git-scm.com/) - Version Control

## 📜 License

Licensed under [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html#license-text).
