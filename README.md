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
1. <del>Dans les fichiers générés, le parseur ne réécrit pas les tags `<![CDATA[` et `]]>` autour des contenus modifiés.</del>
1. <del>les tableaux qui contiennent des balises `<position>` alors qu'ils sont eux-mêmes entourés de balises `<position>`... déconnent à la conversion md -> html, car la balise `<position>` imbriquée n'est pas parsée, mais est présente dans le tableau, et ensuite disparait du tabluea, niquant l'alignement des cellules...</del>

A priori, pas d'autre bug.


# TODO

- Corriger les bugs
