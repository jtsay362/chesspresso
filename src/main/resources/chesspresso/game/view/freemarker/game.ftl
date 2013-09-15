[#ftl]
[#escape contents as contents?html]

[#assign bv = options.bootstrapMajorVersion /]

[#assign imagePrefix = options.imagePrefix!"" /]

[#macro buttonClass]
  [#if bv = 3]
    btn btn-sm
  [#elseif bv = 2]
    btn btn-small
  [#else]
    chesspresso_button
  [/#if]
[/#macro]

[#macro bootstrapIconName kind]
  [#if kind = "start" ][#t]
    fast-backward[#t]
  [#elseif kind = "back" ][#t]
    backward[#t]
  [#elseif kind = "forward" ][#t]
    forward[#t]
  [#elseif kind = "end" ][#t]
    fast-forward[#t]
  [/#if][#t]
[/#macro]

[#macro buttonContents kind]
  [#if bv = 3]
    <span class="glyphicon glyphicon-[@bootstrapIconName kind /]" />
  [#elseif bv = 2]
    <i class="icon-[@bootstrapIconName kind /]" ></i>
  [#else]
    [#if kind = "start" ]
      Start
    [#elseif kind = "back" ]
      &lt;
    [#elseif kind = "forward" ]
      &gt;
    [#elseif kind = "end" ]
      End
    [#else]
      ?
    [/#if]
  [/#if]
[/#macro]

[#macro columnClass numColumns]
  [#compress]
  [#if bv = 3]
    col-md-${numColumns}
  [#elseif bv = 2]
    span${numColumns}
  [#else]
    bootstrap-col-${numColumns}
  [/#if]
  [/#compress]
[/#macro]

[#macro button kind]
  <button id="chesspresso_${kind}_button" type="button" class="[@buttonClass /]" >
    [@buttonContents kind /]
  </button>
[/#macro]

[#if !options.contentOnly]
  <!DOCTYPE html>
  <html>
  <head>
    <meta charset="UTF-8" >
    <meta name="generator" content="Chesspresso" >
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
    [#if game.white??]
      ${game.toString()}
    [/#if]
    </title>

    [#noescape]
    ${options.styleHtml}
    [/#noescape]
  </head>
  <body>
[/#if]

<div class="container-fluid chesspresso_content">
  <div class="row-fluid">
    <div class="[@columnClass 4 /]">
      <div id="chesspresso_container" class="chesspresso_centered">
        <table class="chesspresso_board"><thead></thead>
          <tbody>
            [#assign imageCount = 0 /]
            [#list imagePathsPerRow as imagePathsForRow]
            <tr>
              [#list imagePathsForRow as imagePath]
                <td><img id="chesspresso_square_image_${imageCount}" src="${imagePrefix + imagePath}" alt="${imageCount}" /></td>
                [#assign imageCount = imageCount + 1 /]
              [/#list]
            </tr>
            [/#list]
          </tbody>
        </table>

        <div id="chesspresso_tape_control" class="chesspresso_centered" >
          [@button kind="start" /]
          [@button kind="back" /]
          [@button kind="forward" /]
          [@button kind="end" /]
        </div>
      </div>
    </div>

    <div class="[@columnClass 8 /]">
      [#if game.white??]
        <h4>${game.toString()}</h4>
      [/#if]

      [#compress]
      [#list plys as ply]
        [#if ply.lineStart]&nbsp;([/#if]

        [#assign kind = "line" /]
        [#if ply.level = 0]
          [#assign kind = "main" /]
        [/#if]

        <a id="chesspresso_ply_link_${ply.moveNumber}"
         data-move-number="${ply.moveNumber}"
         class="chesspresso_ply chesspresso_${kind}" href="#">
          [#if ply.showMoveNumber]
            ${ply.plyNumber / 2 + 1}.
          [/#if]

          ${ply.move.toString()}

          [#list ply.nags as nag]
            ${nag}
          [/#list]
        </a>

        [#if ply.comment??]
          <span class="chesspresso_comment">${ply.comment}</span>
        [/#if]

        [#if ply.lineEnd]&nbsp;[/#if]
      [/#list]
      [/#compress]

      [#if game.resultStr??]
        ${game.resultStr}
      [/#if]
    </div>
  </div>

  [#noescape]
  ${options.scriptHtml!""}
  [/#noescape]

  [#noescape]
  <script type="text/javascript">
    // <![CDATA[
    var Chesspresso = function() {
      this.moveNumber = 0;
      [#compress]
      this.imgs = [
        [#assign isFirst = true /]
        [#list imagePaths as imagePath]
          [#if isFirst]
            [#assign isFirst = false /]
          [#else]
            ,
          [/#if]
          '${(imagePrefix + imagePath)?js_string}'
        [/#list]
      ];
      [/#compress]

      [#compress]
      this.sq = [
        [#assign i = 0 /]
        [#list posData as pos]
          [#if i != 0],[/#if]
          [
            [#assign j = 0 /]
            [#list pos as p]
              [#if j != 0],[/#if]${p}
              [#assign j = j + 1 /]
            [/#list]
          ]
          [#assign i = i + 1 /]
        [/#list]
      ];
      [/#compress]

      [#compress]
      this.last = [
        [#assign j = 0 /]
        [#list lastData as p]
          [#if j != 0],[/#if]${p}
          [#assign j = j + 1 /]
        [/#list]
      ];
      [/#compress]

      this.lastMoveNumber = ${lastMoveNumber};
    };

    Chesspresso.prototype.highlightPly = function (selected) {
      if (this.moveNumber > 0) {
        var element = $('#chesspresso_ply_link_' + this.moveNumber);

        if (selected) {
          element.removeClass('chesspresso_deselected_ply_link');
          element.addClass('chesspresso_selected_ply_link');
        } else {
          element.removeClass('chesspresso_selected_ply_link');
          element.addClass('chesspresso_deselected_ply_link');
        }
      }
    }

    Chesspresso.prototype.go = function (num) {
      this.highlightPly(false);

      if (num < 0) {
        this.moveNumber=0;
      } else if (num > this.lastMoveNumber) {
        this.moveNumber = this.lastMoveNumber;
      } else {
        this.moveNumber = num;
      }

      this.highlightPly(true);

      for(var i = 0; i < 64; i++){
        var offset = 0;
        if ((Math.floor(i/8)%2)==(i%2)) offset=0; else offset=13;
        $('#chesspresso_square_image_' + i).attr('src', this.imgs[this.sq[num][i]+offset]);
      }
    }
    Chesspresso.prototype.gotoStart = function() {this.go(0);}
    Chesspresso.prototype.goBackward = function() {this.go(this.last[this.moveNumber]);}
    Chesspresso.prototype.goForward = function() {for (var i=this.lastMoveNumber + 1; i>this.moveNumber; i--) if (this.last[i]==this.moveNumber) {this.go(i); break;}}
    Chesspresso.prototype.gotoEnd = function() {this.go(this.lastMoveNumber);}

    $('#chesspresso_start_button').click(function() {
      chesspresso.gotoStart();
    });

    $('#chesspresso_back_button').click(function() {
      chesspresso.goBackward();
    });

    $('#chesspresso_forward_button').click(function() {
      chesspresso.goForward();
    });

    $('#chesspresso_end_button').click(function() {
      chesspresso.gotoEnd();
    });

    $('.chesspresso_ply').click(function(e) {
      var moveNumber = $(this).attr('data-move-number');
      chesspresso.go(parseInt(moveNumber));
      e.preventDefault();
    });

    var chesspresso = new Chesspresso();
    // ]]>
  </script>
  [/#noescape]

  </div>

[#if !options.contentOnly]
  </body>
</html>
[/#if]
[/#escape]
