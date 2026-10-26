# Petits Génies (KidsLearn) — v6

Application éducative Android pour enfants de 4-5 ans : lettres et chiffres en **français, arabe, anglais** et mode mixte FR+AR.
Aucune publicité, aucun achat intégré, aucune connexion Internet requise (permission : vibration uniquement).

## Nouveautés v6
- **Arabe voyellé (tashkeel)** pour tout ce qui est lu à voix haute : « جَمَل » n'est plus lu « جُمل ».
- **Tracé strict** : trait fin, 90 % des points, et chaque partie du symbole (grille 5x5) doit avoir été visitée avant « Bravo ».
- **Mode « Jouer ! (surprise) »** : l'appli tire les jeux au hasard et change de jeu toutes les 3 étoiles, sans répéter les 4 derniers.
- **Décor illustré animé** (ciel, soleil, nuages, collines, fleurs) sur tous les jeux.
- **Animal compagnon** (choisi sur l'accueil) : il parle dans une bulle, saute de joie, secoue la tête.
- **Glisser-déposer qui suit le doigt** (Tri, Associer, Moitié-moitié, Cache-cache).
- **Cache-cache** : on déplace l'arbre, la voiture, la maison... pour retrouver la lettre / le chiffre / l'animal.
- **Le panier** : un tapis roulant, on choisit les objets de la famille demandée.
- **Petit, moyen, grand** (3 tailles). **Relier** : le mot est lu, on le relie à son image.
- **Moitié-moitié** : compléter l'image. **Animaux** : apprendre, écouter, cache-cache.
- **Quitter ?** : question Oui / Non sur l'accueil. **Version** affichée dans la zone parents (6.0).
- Animations : apparition rebondissante des tuiles, secousse si faux, saut si juste.

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
