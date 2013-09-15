This was forked from Chesspresso 0.9.2, originally written by Bernhard Seybold.
His original docs are at: http://www.chesspresso.org/

This fork has the following improvements:
 * PGN parser
   * Handles file without headers, e.g. (1. e4 d6 2. a3)
   * Better log output
 * HTML generator
   * HTML5 output
   * Safer output (HTML escapes content in game before outputting)
   * Less polution of the Javascript environment, HTML ID's, and CSS class namespace
   * Uses Bootstrap 2 or 3 for fonts, buttons, etc.
   * Style and script section customizable
   * Uses Freemarker to generate output so much easier to modify

