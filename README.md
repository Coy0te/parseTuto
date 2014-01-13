parseTuto
=========

Parseur zCode -> markdown.


# Technos

- développé en Java
- s'appuie sur la lib `org.jsoup` (parseur HTML)


# Utilisation

La seule classe est **TestJSoup.java.**, dans le package `zds.parseTuto`.
Elle prend en entrée un répertoire contenant les fichiers .tuto formatés en zCode, et produit en sortie les mêmes fichiers formatés en markdown (syntaxe évoluée).

Elle est commentée, il suffit de l'exécuter (sous Eclipse, Run As > Java App) pour générer le fichier en sortie.


# Bugs connus

1. Les tableaux ne sont pas entièrement gérés :

- <del>un saut de ligne dans une cellule casse la mise en forme markdown du tableau</del>
- les attributs de cellule `rowspan` et `colspan` ne sont pas bien gérés, pour le moment le contenu d'une cellule spannée est dupliqué dans sa version splitée.

2. Dans les fichiers générés, le parseur ne réécrit pas les tags `<![CDATA[` et `]]>` autour des contenus modifiés.

A priori, pas d'autre bug.


# TODO

- Corriger les bugs
