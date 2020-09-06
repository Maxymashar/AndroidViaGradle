const path = require("path");
module.exports = {
  mode:"development",
  entry:"./js/download/app.js",
  output:{
    path:path.resolve(__dirname,"build"),
    filename:"out.bundle.download.js"
  },
  module:{
    rules:[
      {
        test:/\.js$/,
        exclude:"/node_modules/",
        use:{
          loader:"babel-loader",
          options:{
            presets:["@babel/preset-env"]
          }
        }
      }
    ]
  }
}