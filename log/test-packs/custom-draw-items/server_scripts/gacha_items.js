ServerEvents.generateData('after_mods', event => {
    GachaDrawItems.define(event, 'gacha_demo:script_ticket', {
        card_pool: 'gacha_demo:custom_items',
        package: 'gacha_demo:triple',
        source: {
            gacha_type: 'gachaaddiction:modern_2'
        },
        item: {
            id: 'gachaaddiction:wish_totem',
            components: {
                'minecraft:item_name': '脚本抽奖券',
                'minecraft:custom_model_data': 1
            }
        }
    })
})
