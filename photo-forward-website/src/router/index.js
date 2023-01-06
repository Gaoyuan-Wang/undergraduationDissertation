import Vue from 'vue'
import Router from 'vue-router'
import PhotoUpload from '@/components/PhotoUpload'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'PhotoUpload',
      component: PhotoUpload
    }
  ]
})
