<template>
    <div>
        <Photog-Header></Photog-Header>
        <Album-List></Album-List>
        <Photog-Footer></Photog-Footer>
    </div>
</template>

<script>
import PhotogHeader from './header.vue'
import PhotogFooter from './footer.vue'
import AlbumList from './album-list.vue'

export default {
    name: 'Photog-App',
    props: {

    },
    components: {
        'Photog-Header': PhotogHeader,
        'Photog-Footer': PhotogFooter,
        'Album-List': AlbumList,
    },
    data() {
        return {

        }
    },
    computed: {

    },
    created() {
    },
    methods: {

    }
}
</script>
