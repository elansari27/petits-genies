# Petits Génies : revue senior

## Ce qui était déjà solide

- Projet Compose compact, sans permission Internet, sans publicité ni achats.
- Les interactions enfant sont déjà généreuses : haptique, TTS, confettis, Canvas,
  drag-and-drop et seuil de traçage explicite.
- La séparation `Data`, `Profile`, `SoundManager`, `Ui` et `Games*` est lisible
  pour une base de prototype.

## Corrections livrées dans cette version

- L'accueil a maintenant une action primaire **Jouer**. La langue est un choix
  séparé, au lieu de déclencher immédiatement la navigation.
- Le menu est présenté comme un parcours, avec un titre plus orienté enfant.
- La zone parents utilise un calcul additionnel généré à l'ouverture au lieu du
  code affiché `1234`.
- Le README précise une direction photo sûre : scènes chaleureuses, sujets
  non-identifiables, fonds simples et pas de données personnelles.

## Priorités avant publication

1. Ajouter des tests Compose pour le gate parent, le changement de langue et le
   retour arrière.
2. Remplacer les emojis critiques par des assets vectoriels cohérents pour
   éviter les variations de rendu entre appareils.
3. Ajouter une vraie couche de navigation ou un state machine testable lorsque
   le nombre de mini-jeux dépasse ce prototype.
4. Ajouter `gradlew`, un workflow CI et une vérification `assembleRelease`.
   Le zip fourni ne contenait pas de wrapper Gradle et l'environnement d'analyse
   ne pouvait donc pas compiler localement.
