const path = require('path')

// uni-app 的 vue-loader 依赖其内置的 @vue/component-compiler-utils（支持 recyclableRender 等改造），
// 而 npm 扁平化后默认会解析到官方版本，这里显式指向内置实现。
const moduleAlias = require('module-alias')
moduleAlias.addAlias(
  '@vue/component-compiler-utils',
  path.resolve(__dirname, 'node_modules/@dcloudio/vue-cli-plugin-uni/packages/@vue/component-compiler-utils')
)

module.exports = {
  // uni-app CLI 编译配置，构建入口由 UNI_INPUT_DIR 环境变量指定
  productionSourceMap: false
}
