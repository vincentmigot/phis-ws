import { ApiServiceBinder } from './lib';
import ImageView from './components/images/ImageView.vue';
import ImageSearch from './components/images/ImageSearch.vue';
import ImageList from './components/images/ImageList.vue';
import ImageGrid from './components/images/viewComponents/ImageGrid.vue';
import ImageSingle from './components/images/viewComponents/ImageSingle.vue';
import ImageCarousel from './components/images/viewComponents/ImageCarousel.vue';
import SciObjectURISearch from './components/images/searchComponents/SciObjectURISearch.vue';
import SciObjectAliasSearch from './components/images/searchComponents/SciObjectAliasSearch.vue';
import ImageTypeSearch from './components/images/searchComponents/ImageTypeSearch.vue';
import ExperimentSearch from './components/images/searchComponents/ExperimentSearch.vue';
import SciObjectTypeSearch from './components/images/searchComponents/SciObjectTypeSearch.vue';
import TimeSearch from './components/images/searchComponents/TimeSearch.vue';

import { library } from '@fortawesome/fontawesome-svg-core'
import { faSlidersH, faSearch, faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
library.add(faSlidersH,faSearch,faChevronDown,faChevronUp);
export default {
    install(Vue, options) {
        ApiServiceBinder.with(Vue.$opensilex.getServiceContainer());
        // TODO register components
    },
    components: {
        'phis2ws-ImageView': ImageView,
        'phis2ws-ImageSearch': ImageSearch,
        'phis2ws-ImageList': ImageList,
        'phis2ws-ImageGrid': ImageGrid,
        'phis2ws-ImageSingle': ImageSingle,
        'phis2ws-ImageCarousel': ImageCarousel,
        'phis2ws-SciObjectURISearch': SciObjectURISearch,
        'phis2ws-SciObjectAliasSearch': SciObjectAliasSearch,
        'phis2ws-ImageTypeSearch': ImageTypeSearch,
        'phis2ws-ExperimentSearch': ExperimentSearch,
        'phis2ws-SciObjectTypeSearch': SciObjectTypeSearch,
        'phis2ws-TimeSearch': TimeSearch
    }
};
