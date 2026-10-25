# Petits Génies (KidsLearn) — v5

Application éducative Android pour enfants de 4-5 ans : lettres et chiffres en **français, arabe, anglais** et mode mixte FR+AR.
Aucune publicité, aucun achat intégré, aucune connexion Internet requise (permission : vibration uniquement).

## Nouveautés v5
- **Voix arabe avancée** : moteur Google TTS demandé en priorité (repli sur le moteur du téléphone), choix automatique de la meilleure voix installée par langue (hors-ligne, haute qualité), découpage du texte par écriture, chiffres lus en mots dans la langue de la phrase, déclaration `<queries>` pour que Android 11+ voie les moteurs vocaux.
- **Tracé** : plus de « Bravo » prématuré : le doigt doit réellement passer sur 85 % du trait et couvrir la hauteur et la largeur du symbole.
- **Mascotte originale Zizou** (dessinée dans l'appli) sur l'accueil.
- **Menu en hexagones** (nid d'abeille).
- **Nouveaux jeux** : Trouver l'intrus, Tri, Logique, Puzzles-ombres.
- Confettis du cache-cache corrigés (s'affichent sur tout l'écran).

## Nouveautés v4
- **Chiffres européens uniquement** (0 1 2 3…) dans toutes les langues, y compris en arabe.
- **Anglais** ajouté (lettres, chiffres, histoires, félicitations, voix).
- **Voix corrigée** : le texte est nettoyé (emojis retirés) et découpé par écriture ; l'arabe est lu avec la voix arabe, le latin avec la voix française/anglaise. Plus de phrases arabes mal lues.
- **Test après apprentissage** : bouton 🎯 Test sur chaque écran d'apprentissage (lettres, chiffres).
- **Cache-cache** (lettres et chiffres) : l'enfant retrouve ce qu'il a appris derrière les décors.
- **Bug اربط corrigé** : les jeux géométriques (relier, associer, panier, tracer, trésor, bulles) restent en gauche-droite ; Compose inversait les positions en arabe.
- **Tracé corrigé** : grille adaptée à la taille du symbole, 75 % suffisent (60 % si l'enfant lève le doigt), points orange d'aide.
- **Zone parents** protégée par une addition au hasard (plus de code fixe).
- Sélecteur de langue Français / Arabe / English / Français+Arabe.

## Conception pédagogique
Découvre → Teste → Cache-cache → Récompense. Maximum 3 choix, tout est lu à voix haute, félicitations avec le prénom, aide après erreurs.

## Compiler
Poussez le dépôt sur GitHub : Actions → « Build APK » → téléchargez `PetitsGenies-apk`.
