[#ftl]
[#escape contents as contents?html]
[#if !contentOnly]
  <!doctype html>
  <html>
  <head>
    <meta name="generator" content="Chesspresso" />
    <title>${game.toString()}</title>
    [#noescape]
    ${styleHtml}
    [/#noescape]
  </head>
  <body>
[/#if]

<div class="chesspresso_content">
  <table class="content">
    <thead></thead>
    <tbody>
      <tr>
        <td valign="top">
          <table cellspacing="0" cellpadding="0"><thead></thead>
            <tbody>
              [#list imagePathsPerRow as imagePathsForRow]
              <tr>
                [#list imagePathsForRow as imagePath]
                  <td><img src="${imagePath}" /></td>
                [/#list]
              </tr>
              [/#list]
            </tbody>
          </table>
          <center><form name="tapecontrol">
            <input type=button value=" Start " onClick="chesspresso.gotoStart();" onDblClick="chesspresso.gotoStart();">
            <input type=button value=" &lt; " onClick="chesspresso.goBackward();" onDblClick="chesspresso.goBackward();">
            <input type=button value=" &gt; " onClick="chesspresso.goForward();" onDblClick="chesspresso.goForward();">
            <input type=button value=" End " onClick="chesspresso.gotoEnd();" onDblClick="chesspresso.gotoEnd();">
            </form>
          </center>
        </td>
        <td valign="top">
          <h4>${game.toString()}</h4>


          [#list plys as ply]
            [#if ply.lineStart]&nbsp;([/#if]

            [#assign kind = "line" /]
            [#if ply.level = 0]
              [#assign kind = "main" /]
            [/#if]

            <a name="${ply.moveNumber}" class="${kind}"
             href="javascript:chesspresso.go(${ply.moveNumber})">
              [#if ply.showMoveNumber]
                ${ply.plyNumber / 2 + 1}.
              [/#if]

              ${ply.move.toString()}

              [#list ply.nags as nag]
                ${nag}
              [/#list]
            </a>

            [#if ply.comment??]
              <span class="comment">${ply.comment}</span>
            [/#if]

            [#if ply.lineEnd]&nbsp;[/#if]

          [/#list]

          ${game.resultStr}
        </td>
      </tr>
    </tbody>
  </table>

  [#noescape]
  <script type="text/javascript">
    // <![CDATA[
    var Chesspresso = function() {
      this.moveNumber = 0;
      this.imgs = [
        [#assign isFirst = true /][#t]
        [#list imagePaths as imagePath][#t]
          [#if isFirst][#t]
            [#assign isFirst = false /][#t]
          [#else]
            ,[#t]
          [/#if][#t]
          '${imagePath?js_string}'[#t]
        [/#list][#t]
      ];

      this.sq = [
        [#assign i = 0 /]
        [#list posData as pos]
          [#if i != 0],[/#if][#t]
          [
            [#assign j = 0 /][#t]
            [#list pos as p][#t]
              [#if j != 0],[/#if]${p}[#t]
              [#assign j = j + 1 /][#t]
            [/#list][#t]
          ]
          [#assign i = i + 1 /][#t]
        [/#list]
      ];

      this.last = [
        [#assign j = 0 /][#t]
        [#list lastData as p][#t]
          [#if j != 0],[/#if]${p}[#t]
          [#assign j = j + 1 /][#t]
        [/#list][#t]
      ];

      this.lastMoveNumber = ${lastMoveNumber};
    };

    Chesspresso.prototype.go = function (num) {
      if (this.moveNumber>0) {window.document.anchors[this.moveNumber-1].style.background="white"; window.document.anchors[this.moveNumber-1].style.color="black";}
      if (num<0) this.moveNumber=0;
      else if (num > this.lastMoveNumber) this.moveNumber = this.lastMoveNumber;
      else this.moveNumber=num;
      for(i=0;i<64;i++){
        if ((Math.floor(i/8)%2)==(i%2)) offset=0; else offset=13;
        window.document.images[i].src=this.imgs[this.sq[num][i]+offset];
      }
      if (this.moveNumber>0) {window.document.anchors[this.moveNumber-1].style.background="black"; window.document.anchors[this.moveNumber-1].style.color="white";}
    }
    Chesspresso.prototype.gotoStart = function() {this.go(0);}
    Chesspresso.prototype.goBackward = function() {this.go(this.last[this.moveNumber]);}
    Chesspresso.prototype.goForward = function() {for (i=this.lastMoveNumber + 1; i>this.moveNumber; i--) if (this.last[i]==this.moveNumber) {this.go(i); break;}}
    Chesspresso.prototype.gotoEnd = function() {this.go(this.lastMoveNumber);}
    var chesspresso = new Chesspresso();
    // ]]>
  </script>
  [/#noescape]

  </div>

[#if !contentOnly]
  </body>
</html>
[/#if]
[/#escape]