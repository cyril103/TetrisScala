Fin de session :
- Plus aucun warning sbt : Compile / mainClass := Some("tetris.Main").
- Gravité suspendue jusqu’au clic sur Start Game, restart via R relance correctement.
- Slider volume débounce (écriture config en fin de glissement).
- Tests : sbt test (18/18 OK).
TODO:
- Ajouter effets audio (sfx) avec contrôle par le slider de volume.
- Créer d’autres thèmes HUD (clair, rétro) au-delà du switch darkMode.
- Implémenter Hard Drop + lock delay configurable.
- Sauvegarder un leaderboard local (top 5) avec initiales.
- Afficher des stats avancées (lignes restantes, vitesse, combos).
