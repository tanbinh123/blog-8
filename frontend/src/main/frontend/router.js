import Vue from 'vue'
import Router from 'vue-router'
import UserProfile from './components/UserProfile.vue'
import NotFoundComponent from './components/NotFoundComponent.vue'
import UserList from './components/UserList.vue'
import Autocomplete from './components/Autocomplete.vue'
import Registration from './components/Registration.vue'
import PostList from './components/PostList.vue'
import PostView from './components/PostView.vue'
import createPostDto from './factories/PostDtoFactory'
const PostEdit = () => import('./components/PostEdit.vue');

// This installs <router-view> and <router-link>,
// and injects $router and $route to all router-enabled child components
Vue.use(Router);

const root = '/';
const users = '/users';
const usersWithPage = users + '/:page?'; // '?' means not necessary as in RegExp
const useProfileName = 'user-profile';
const post = 'post';

const router = new Router({
    mode: 'history',
    routes: [
        {
            path: root,
            component: PostList
        },
        { name: useProfileName, path: '/user/:id?', component: UserProfile, props: true, },
        // { path: usersWithPage, component: UserList, name: "users", props: true },
        { path: users, component: UserList},
        { path: '/autocomplete', component: Autocomplete},
        { path: '/registration', component: Registration },
        { path: '/post/add', component: PostEdit, props: {postDTO: createPostDto(), onAfterSubmit: (savedOnServerPostDto)=>{
            router.push({ name: post, params: { id: savedOnServerPostDto.id }})
        }}},
        { name: post, path: '/post/:id', component: PostView},
        { path: '*', component: NotFoundComponent },
    ]
});


export  {
    router as default,
    root,
    users,
    usersWithPage,
    useProfileName,
    post
}