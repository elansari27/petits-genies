# Petits Génies — v3 (adapté 4-5 ans, Ilyas)

Appli d'apprentissage gratuit, sans pub et sans achat, pour enfant de 4 à 5 ans.
12 jeux + récompenses : étoiles, autocollants, série de jours 🔥, confettis 🎉,
encouragements prononcés avec le prénom de l'enfant.

## v3 — parcours enfant + garde-fou parents
- 🧭 **Accueil clarifié** : bouton Jouer prioritaire, choix de langue séparé,
  parcours de mini-jeux coloré et lisible.
- 🔒 **Zone parents** : remplacement du code fixe `1234` par un calcul aléatoire
  à chaque ouverture, plus adapté à un espace adulte sans exposer un secret dans l'UI.
- 🐛 **Corrigé : le traçage disait "Bravo" trop tôt.** La zone de détection était trop
  large (1 tap = ~11 points couverts) et le seuil n'était que de 80%. Maintenant :
  zone réduite, échantillonnage plus fin, **90% de points réellement couverts**,
  et une **barre de progression** montre à l'enfant qu'il doit finir le tracé.
- 🐘 **Petit ou grand** : comparer les tailles (touche le plus grand / le plus petit)
- 🔗 **Relier** : tracer un trait à la main de la lettre vers l'image (glisser)
- 🧺 **Le panier** : glisser-déposer les objets dans la corbeille
- 🎉 **Confettis** à chaque victoire

## Jeux déjà présents (v2)
Lettres FR/AR, chiffres, couleurs, formes, compter, première lettre, memory,
quiz écoute (3 propositions pour 4-5 ans), tracer, associer, histoires,
bulles magiques, chasse au trésor, boutique d'autocollants.

## Zone parents (code 1234)
Stats (étoiles, autocollants, série de jours), profil de l'enfant
(prénom, avatar, âge), installation de la voix arabe, remise à zéro.

## Techniques
Jetpack Compose (BOM 2024.06), Material 3, gestes tactiles personnalisés
(awaitEachGesture, detectDragGestures), Canvas (formes, lignes, confettis),
TTS français/arabe, synthèse audio PCM, haptique, SharedPreferences.

## Direction visuelle
Palette chaude et contrastée, mascotte expressive, grandes cibles tactiles et
cartes rondes. Pour les visuels futurs : photos d'enfants non identifiables en
interaction avec un adulte, objets simples sur fonds unis, lumière naturelle,
aucun écran montrant de données personnelles. Les photos n'étaient pas présentes
dans le zip reçu, donc l'interface reste autonome avec emojis et formes Canvas.
