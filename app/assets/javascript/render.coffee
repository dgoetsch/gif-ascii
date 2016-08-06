$ ->
  framesString = $("#data")
  console.log(framesString)
  frames = $.parseJSON(framesString.html())
  curIndex = 0
  getNextIndex= -> if curIndex >= frames.length then curIndex = 0 else curIndex++
  displayNextFrame = -> $("#gifBody").html(frames[getNextIndex()])
  callback = displayNextFrame.bind(this)
  myInterval = window.setInterval(callback, 100)