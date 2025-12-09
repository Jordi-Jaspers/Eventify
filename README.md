<p align="center">
    <h1 align="center">Eventify</h1>
</p>
<p align="center">Eventify.io - An intuitive tool to manage and monitor your services via intelligent event creation.</p>
<p align="center">
    <img src="https://github.com/Jordi-Jaspers/Eventify/actions/workflows/pipeline.yml/badge.svg?branch=develop" alt="Pipeline" >    
</p>
<p align="center">
    <img alt="license" src="https://img.shields.io/github/license/Jordi-Jaspers/Eventify"> 
    <img alt="GitHub release (with filter)" src="https://img.shields.io/github/v/release/Jordi-Jaspers/Eventify?sort=semver">
    <img alt="GitHub Issues or Pull Requests" src="https://img.shields.io/github/issues/Jordi-Jaspers/Eventify?color=red">
    <img src="https://img.shields.io/github/commit-activity/m/Jordi-Jaspers/Eventify" alt="Commit Activity" >
    <img src="https://img.shields.io/github/last-commit/Jordi-Jaspers/Eventify" alt="Last Commit" >
</p>

---

**Author:** Jordi
Jaspers [[Github](https://github.com/Jordi-Jaspers "Github Page"), [Linkedin](https://www.linkedin.com/in/jordi-jaspers/ "Linkedin Page")]
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

## Introduction 📝

A high-performance, scalable monitoring solution designed to track and visualize the health and status of distributed
systems in real-time. This application provides a robust platform for collecting, processing, and displaying monitoring
events from multiple external systems through a user-friendly dashboard interface.

**Website Preview:** https://eventify.io/  
**Website Health endpoint:** https://eventify.io/actuator/health  
**Server API description:** https://eventify.io/api/public/docs/openapi.html

### Overview

The Monitoring Service is built to handle high-throughput event processing with real-time updates, making it ideal for
organizations needing to monitor critical infrastructure and services. It features:

- **Real-time Monitoring**: Instant visibility into system health through WebSocket-powered live updates
- **Scalable Architecture**: Horizontally scalable design using event-driven microservices
- **Efficient Event Processing**: Smart batching and processing of events to handle high-volume monitoring data
- **Team-based Access Control**: Organized access management with team-based dashboard sharing
- **Customizable Dashboards**: Flexible dashboard configuration with support for grouped and ungrouped checks
- **High Availability**: Built on a reliable PostgreSQL cluster with replication for data durability
- **External System Integration**: Simple REST API for external systems to report their status

Whether you're monitoring a handful of services or thousands of endpoints, this platform is designed to provide
reliable, real-time insights into your systems' operational status.

**Want to start up the application?**
read more in the getting started section -> [here](documentation/getting_started.md)

**Want to see some concept or add ideas?**
read more in the concepts section -> [here](documentation/concepts.md)

## Frontend Development 💻

The Eventify frontend is built with modern web technologies for a fast, responsive, and type-safe user experience.

### Tech Stack

- **SvelteKit** 2.47.1 - Full-stack framework with SSR and CSR
- **Svelte** 5.41.0 - Reactive component framework
- **Bun** 1.3.0 - Fast JavaScript runtime and package manager
- **TailwindCSS** 4.1.17 - Utility-first CSS framework (v4)
- **shadcn-svelte** - High-quality, accessible component library
- **TypeScript** 5.9.3 - Strict type safety
- **openapi-fetch** - Type-safe API client generated from backend OpenAPI spec

### Quick Start

```bash
# Navigate to the client directory
cd client

# Install dependencies
bun install

# Set up environment variables
cp .env.example .env

# Generate TypeScript types from backend API (requires backend running)
bun run generate:api

# Start development server
bun run dev
```

The frontend will be available at http://localhost:5173/

### Key Features

- **Type-Safe API Integration**: TypeScript types automatically generated from backend OpenAPI specification
- **Real-time Updates**: WebSocket support for live monitoring data
- **Component Library**: shadcn-svelte for consistent, accessible UI components
- **Authentication**: JWT-based authentication with secure token storage
- **Responsive Design**: Mobile-first approach with TailwindCSS utilities
- **Developer Experience**: Hot module replacement, fast refresh, and comprehensive type checking

### Documentation

For detailed frontend development guidelines, see [docs/frontend-development.md](docs/frontend-development.md).

This includes:
- Complete setup instructions
- Development workflow and best practices
- Component development guidelines
- API integration patterns
- Testing strategies
- Deployment instructions

## Dang, you found a bug, now what? 🐞

No worries, just check out our [**issues**](https://github.com/Jordi-Jaspers/Eventify) section on GitHub. If you can't
find your issue, feel free to create a new one. We trample that bug as soon as possible.

## Support & Contributions ☕️

#### Want to contribute to the project?

All the extra help is welcome! Just follow the steps in the [**CONTRIBUTING**](CONTRIBUTING.md) section.

#### Any more ideas for features?

Feel free to create an issue, and we can discuss it further or contact me directly.

## Stack 🛠️

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

## License 📜

Licensed under [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html#license-text).
