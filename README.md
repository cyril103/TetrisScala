# TetrisScala

Un clone du jeu Tetris classique (style NES/Game Boy) développé en Scala 3 et JavaFX.
Ce projet a été entièrement généré par une IA.

## Fonctionnalités

- Grille de jeu 10x20.
- 7 tétriminos avec leurs couleurs classiques.
- Génération de pièces aléatoire de type "sac de 7" pour une distribution équitable.
- Déplacements (gauche/droite), rotations (horaire/anti-horaire) et soft drop.
- Pas de "hold piece" ni de "ghost piece" pour une expérience fidèle à l'original.
- Augmentation de la vitesse progressive basée sur les niveaux.
- Nettoyage de lignes et effondrement de la pile.
- Calcul du score et des niveaux selon le barème classique.
- Écran de Game Over avec possibilité de redémarrer.

## Prérequis

Pour compiler et exécuter ce projet, vous aurez besoin de :

- **JDK 17** ou une version ultérieure (LTS recommandée).
- **sbt** (Simple Build Tool) version 1.9.x ou ultérieure.

## Commandes sbt

Placez-vous à la racine du projet pour exécuter les commandes suivantes.

### Compiler le projet

```bash
sbt compile
```

### Lancer le jeu

```bash
sbt run
```

### Nettoyer les fichiers compilés

```bash
sbt clean
```

## Commandes du jeu

| Touche(s)                 | Action                     |
| ------------------------- | -------------------------- |
| `Flèche Gauche` / `A`     | Déplacer la pièce à gauche |
| `Flèche Droite` / `D`     | Déplacer la pièce à droite |
| `Flèche Bas` / `S`        | Accélérer la descente      |
| `Flèche Haut` / `W` / `X` | Rotation (sens horaire)    |
| `Z` / `Ctrl`              | Rotation (sens anti-horaire) |
| `R`                       | Recommencer (après un Game Over) |

## Règles et Score

- **But du jeu** : Complétez des lignes horizontales pour les faire disparaître et marquer des points.
- **Niveaux** : Le niveau augmente toutes les 10 lignes complétées. Chaque niveau augmente la vitesse de chute des pièces.
- **Score** : Le score est calculé en fonction du nombre de lignes complétées en un seul coup et du niveau actuel.
  - 1 ligne (Single) : `40 * (Niveau + 1)`
  - 2 lignes (Double) : `100 * (Niveau + 1)`
  - 3 lignes (Triple) : `300 * (Niveau + 1)`
  - 4 lignes (Tetris) : `1200 * (Niveau + 1)`
- **Game Over** : La partie se termine si une nouvelle pièce ne peut pas apparaître en haut de la grille car l'espace est bloqué.

## Crédits

- **Développement** : Gemini, une IA de Google.
- **Technologies** : Scala 3, JavaFX, sbt.
