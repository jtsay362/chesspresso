/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id: HTMLGameBrowser.java,v 1.3 2003/01/04 16:23:32 BerniMan Exp $
 */

package chesspresso.game.view;

import chesspresso.*;
import chesspresso.game.*;
import chesspresso.move.*;
import chesspresso.position.*;

import java.io.*;
import java.util.Stack;


/**
 * Producer for HTML pages displaying a game.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public class HTMLGameBrowser implements GameListener
{
    
    private StringBuffer m_moves;
    private StringBuffer m_posData;
    private StringBuffer m_lastData;
    private Game m_game;
    private int m_moveNumber;
    private boolean m_showMoveNumber;
    private int[] m_lasts;
    private PrintStream m_out;

    
    //======================================================================
    // GameListener Methods
    
    public void notifyLineStart(int level)
    {
        m_moves.append(" (");
        m_showMoveNumber = true;
        m_lasts[level + 1] = m_lasts[level];
    }
    
    public void notifyLineEnd(int level)
    {
        m_moves.append(") ");
        m_showMoveNumber = true;
    }

    private void addPosData(ImmutablePosition pos)
    {
        m_posData.append("  this.sq[" + m_moveNumber + "] = new Array(");
        for (int row = Chess.NUM_OF_ROWS-1; row >= 0; row--) {
            for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
                int sqi = Chess.coorToSqi(col, row);
                if (sqi != Chess.A8) m_posData.append(",");
                m_posData.append(pos.getStone(sqi) - Chess.MIN_STONE);
            }
        }
        m_posData.append(");\n");
    }

    public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level)
    {
        ImmutablePosition pos = m_game.getPosition();
        
        boolean isMainLine = (level == 0);
        String type = isMainLine ? "main" : "line";
        
        m_moves.append("<a name=\"" + m_moveNumber + "\" class=\"" + type + "\" href=\"javascript:chesspresso.go(" + m_moveNumber + ")\">");
        if (m_showMoveNumber) {
            m_moves.append((plyNumber / 2 + 1) + ".");
        }
        m_showMoveNumber = Chess.isWhitePly(plyNumber+1);
        
        m_moves.append(move.toString());
        if (nags != null) {
            for (int i=0; i<nags.length; i++) {
                m_moves.append(NAG.getShortString(nags[i]));
            }
            m_showMoveNumber = true;
        }
        m_moves.append("</a> ");
        if (comment != null) {
            m_moves.append("<span class=\"comment\">").append(comment).append("</span> ");
        }
        
        addPosData(pos);
        m_lastData.append(",").append(m_lasts[level]);
        m_lasts[level] = m_moveNumber;
        
        m_moveNumber++;
    }

    //======================================================================
    
    private String[] m_wimgs;
    private String[] m_bimgs;
    private String m_imagePrefix;
    private String m_styleFilename;
    
    //======================================================================
    
    /**
     * Create a new HTMLGameBrowser with default settings.
     */
    public HTMLGameBrowser()
    {
        m_wimgs = new String[] {
            "wkw.gif", "wpw.gif", "wqw.gif", "wrw.gif", "wbw.gif", "wnw.gif", "now.gif",
            "bnw.gif", "bbw.gif", "brw.gif", "bqw.gif", "bpw.gif", "bkw.gif"
        };
        m_bimgs = new String[] {
            "wkb.gif", "wpb.gif", "wqb.gif", "wrb.gif", "wbb.gif", "wnb.gif", "nob.gif",
            "bnb.gif", "bbb.gif", "brb.gif", "bqb.gif", "bpb.gif", "bkb.gif"
        };
        m_imagePrefix = "";
        m_styleFilename = null;
    }
    
    //======================================================================
    
    /**
     * Set the name of the style file. If name is set to null, inline style
     * definition will be used. Default is inline style.<br>
     * When using an external style file, the following styles are expected:
     * <ul>
     *  <li>a.main: the anchor used for moves in the main line
     *  <li>a.line: the anchor used for moves in side-lines
     *  <li>span.comment: used for move comments
     *  <li>table.content: the content table containing the board left and the moves on the right
     * </ul>
     *
     *@param styleFilename the name of the style file
     */
    private void setStyleFilename(String styleFilename)
    {
        m_styleFilename = styleFilename;
    }
    
    /**
     * Set thes prefix for images. The default is empty.
     *
     *@param imagePrefix the prefix for images, must contain trailing slashes
     */
    private void setImagePrefix(String imagePrefix)
    {
        m_imagePrefix = imagePrefix;
    }
    
    /**
     * Sets the name of an square image. The default names are set according to
     * the following scheme: First letter is the color of the stone (b, w), second
     * letter the piece (k, q, r, b, n, p) third letter the square color (b, w),
     * extension is gif. now.gif and nob.gif are used for empty squares.<br>
     * For instance: wkw.gif determines a white king on a white square,
     * bbb.gif is a black bishop on a black square.
     *
     *@param stone the stone displayed
     *@param whiteSquare whether or not the square is white
     *@param name the name of the corresponding image
     */
    private void setStoneImageName(int stone, boolean whiteSquare, String name)
    {
        if (whiteSquare) {
            m_wimgs[stone - Chess.MIN_STONE] = name;
        } else {
            m_bimgs[stone - Chess.MIN_STONE] = name;
        }
    }
    
    /**
     * Returns the name of the image.
     *
     *@param stone the stonbe displayed
     *@param whiteSquare whether or not the square is white
     */
    private String getImageForStone(int stone, boolean isWhite)
    {
        return m_imagePrefix + (isWhite ? m_wimgs[stone - Chess.MIN_STONE] : m_bimgs[stone - Chess.MIN_STONE]);
    }
    
    //======================================================================
    
    /**
     * Produces HTML to display a game.
     *
     *@param outStream where the HTML will be sent to
     *@param game the game to display.
     */
    public void produceHTML(OutputStream outStream, Game game)
    {
        produceHTML(outStream, game, false);
    }
    
    /**
     * Produces HTML to display a game.
     *
     *@param outStream where the HTML will be sent to
     *@param game the game to display.
     *@param contentOnly if true skip header and footer information, use this if you want to
     *       produce your own header and footer
     */
    public synchronized void produceHTML(OutputStream outStream, Game game, boolean contentOnly)
    {
        m_out = new PrintStream(outStream);
        
        m_moves = new StringBuffer();
        m_posData = new StringBuffer();
        m_lastData = new StringBuffer();
        m_game = game;
        m_moveNumber = 0;
        m_showMoveNumber = true;
        m_lasts = new int[100]; m_lasts[0] = 0;
        
        m_posData.append("  this.sq = new Array(" + game.getNumOfPlies() + "); ");
        m_lastData.append("  this.last = new Array(0");
        
        m_game.gotoStart();
        addPosData(m_game.getPosition());
        m_moveNumber++;
        
        // FIXME: HTML escape
        m_moves.append("<h4>" + m_game + "</h4>");
        
        game.traverse(this, true);
        
        m_moves.append(" " + game.getResultStr());
        m_lastData.append(");");
        
        if (!contentOnly) {
            p("<!doctype html>");            
            p("<html>");
            p("<head>");
            p("<meta name=\"generator\" content=\"Chesspresso\" />");
            
            // FIXME: HTML escape
            p("<title>" + m_game + "</title>");
            if (m_styleFilename == null) {
                p("<style type=\"text/css\">");
                p("   .main {text-decoration:none}");
                p("   .line {text-decoration:none}");
                p("  a.main {font-weight:bold; color:black}");
                p("  a.line {color:black}");
                p("  table.content {cell-spacing:20}");
                p("  span.comment {font-style:italic}");
                p("</style>");
            } else {
                p("<link rel=\"stylesheet\" href=\"" + m_styleFilename + "\" type=\"text/css\" />");
            }
        
            p("<script>");
            p("var Chesspresso = function() {");
            p("  this.moveNumber = 0;");
            pnl("  this.imgs = new Array(");
            
            for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
                pnl("'" + getImageForStone(stone, true) + "',");
            }
            
            for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
                pnl("'" + getImageForStone(stone, false) + "'");
                if (stone < Chess.MAX_STONE) pnl(",");
            }
            
            p(");");            
            p(m_posData.toString());                                            
            p(m_lastData.toString());            
            p("};");
        
//        p("function go(num) {window.document.anchors[moveNumber-1].style.background=\"white\"; if (num<0) moveNumber=0; else if (num>" + (m_moveNumber - 1) + ") moveNumber=" + (m_moveNumber - 1) + "; else moveNumber=num; for(i=0;i<64;i++){if ((Math.floor(i/8)%2)==(i%2)) window.document.images[i].src=wimgs[sq[num][i]]; else window.document.images[i].src=bimgs[sq[num][i]];}; window.document.anchors[moveNumber-1].style.background=\"black\";}");
            p("  Chesspresso.prototype.go = function (num) {");
            // TODO style for selected move
            p("    if (this.moveNumber>0) {window.document.anchors[this.moveNumber-1].style.background=\"white\"; window.document.anchors[this.moveNumber-1].style.color=\"black\";}");
            p("    if (num<0) this.moveNumber=0;");
            p("    else if (num>" + (m_moveNumber - 1) + ") this.moveNumber=" + (m_moveNumber - 1) + ";");
            p("    else this.moveNumber=num;");
            p("    for(i=0;i<64;i++){");
            p("      if ((Math.floor(i/8)%2)==(i%2)) offset=0; else offset=13;");
            p("      window.document.images[i].src=this.imgs[this.sq[num][i]+offset];");
            p("    }");
            p("    if (this.moveNumber>0) {window.document.anchors[this.moveNumber-1].style.background=\"black\"; window.document.anchors[this.moveNumber-1].style.color=\"white\";}");
            p("  }");
            p("  Chesspresso.prototype.gotoStart = function() {this.go(0);}");
            p("  Chesspresso.prototype.goBackward = function() {this.go(this.last[this.moveNumber]);}");
            p("  Chesspresso.prototype.goForward = function() {for (i=" + m_moveNumber + "; i>this.moveNumber; i--) if (this.last[i]==this.moveNumber) {this.go(i); break;}}");
            p("  Chesspresso.prototype.gotoEnd = function() {this.go(" + (m_moveNumber - 1) + ");}");            
            
            p("var chesspresso = new Chesspresso();");
            
            p("</script>");
            p("");

            p("</head>");
            p("");

            p("<body>");
        }
        
        p("<table class=\"content\"><thead></thead><tbody><tr><td valign=\"top\">");
        
        p("<table cellspacing=\"0\" cellpadding=\"0\"><thead></thead><tbody>");
        Position startPos = Position.createInitialPosition();
        for (int row = Chess.NUM_OF_ROWS-1; row >= 0; row--) {
            pnl("  <tr>");
            for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
                int sqi = Chess.coorToSqi(col, row);           
                pnl("<td><img src=\"" + getImageForStone(startPos.getStone(sqi), Chess.isWhiteSquare(sqi)) + "\"></td>");
            }
            p("</tr>");
        }
        p("</tbody>");
        p("</table>");
        p("<center><form name=\"tapecontrol\">");
        p("<input type=button value=\" Start \" onClick=\"chesspresso.gotoStart();\" onDblClick=\"chesspresso.gotoStart();\">");
        p("<input type=button value=\" &lt; \" onClick=\"chesspresso.goBackward();\" onDblClick=\"chesspresso.goBackward();\">");
        p("<input type=button value=\" &gt; \" onClick=\"chesspresso.goForward();\" onDblClick=\"chesspresso.goForward();\">");
        p("<input type=button value=\" End \" onClick=\"chesspresso.gotoEnd();\" onDblClick=\"chesspresso.gotoEnd();\">");
        p("</form></center>");
        p("");
        
        p("</td><td valign=\"top\">");
        // FIXME: HTML Escape
        p(m_moves.toString());
        p("</td></tr></tbody></table>");
        
        if (!contentOnly) {
            p("</body></html>");
        }
    }
    
    public void pnl(String s) {
      m_out.print(s);
    }
    
    public void p(String s) {
      m_out.println(s);
    }
    
    public static void main(String[] args)
    {
        try {
            chesspresso.pgn.PGNReader pgn = new chesspresso.pgn.PGNReader(
                new FileReader(args[0]), "game");
            Game game = pgn.parseGame();

            HTMLGameBrowser html = new HTMLGameBrowser();
            html.produceHTML(System.out, game);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
