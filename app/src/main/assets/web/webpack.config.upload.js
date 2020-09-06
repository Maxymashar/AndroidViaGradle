const path = require("path");
module.exports = {
  mode:"development",
  entry:"./js/upload/app.js",
  output:{
    path:path.resolve(__dirname,"build"),
    filename:"out.bundle.upload.js"
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