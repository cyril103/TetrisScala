# AGENTS

Ce projet TetrisScala combine un developpeur humain et plusieurs agents d'IA. Ce document recense les agents actifs, leurs responsabilites et la facon dont ils interviennent dans le projet.

## Agents actifs

| Agent | Type | Responsabilites principales | Contact |
| --- | --- | --- | --- |
| Utilisateur humain | Developpeur | Pilotage global, decisions de conception, validations finales. | Proprietaire du depot |
| Gemini | IA generative (Google) | Generation initiale du code et configuration du squelette du projet. | Historique |
| Codex (GPT-5) | IA generative (OpenAI) | Assistance ponctuelle, revues, documentation, taches de support. | CLI Codex |

## Principes de collaboration

1. Prioriser la tracabilite des decisions importantes via commits ou documents.
2. Valider toute modification fonctionnelle par l'utilisateur humain avant diffusion.
3. Documenter les interactions complexes (refactorings, choix techniques) dans `docs/` ou dans les fichiers concernes.

## Historique

- Creation initiale du projet par Gemini (voir README.md).
- Documentation `AGENTS.md` redigee pour clarifier les responsabilites des agents.

## Ressources

- `README.md` : instructions d'installation et de lancement.
- `docs/` : emplacement recommande pour les notes techniques et les comptes rendus d'agents.
