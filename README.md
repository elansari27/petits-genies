# Petits Génies — v3 (adapté 4-5 ans, Ilyas)

Appli d'apprentissage gratuit, sans pub et sans achat, pour enfant de 4 à 5 ans.
12 jeux + récompenses : étoiles, autocollants, série de jours 🔥, confettis 🎉,
encouragements prononcés avec le prénom de l'enfant.

## v3 — corrections et nouveaux jeux "à la Play Store"
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
