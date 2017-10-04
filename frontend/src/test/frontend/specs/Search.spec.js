import Vue from 'vue'
import Search from "../../../main/frontend/components/Search.vue"
import { mount } from 'avoriaz';

describe('Search.vue', ()=>{
    let wrapper;

    beforeEach(()=>{
        wrapper = mount(Search);
        expect(wrapper).toBeDefined();
    });
    afterEach(()=>{

    });

    it('emit event', (done)=>{
        wrapper.vm.$on('SEARCH_EVENT', (str)=>{
            expect(str).toBe('good day for die');
            done();
        });

        const el = wrapper.find("input#search")[0];
        el.element.value = 'good day for die';
        el.trigger('input');
    });
});