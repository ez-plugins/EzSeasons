## Summary

- What does this PR change?
- Why is this needed?

## Type of change

- [ ] Bug fix
- [ ] Feature
- [ ] Documentation
- [ ] Refactor
- [ ] Breaking change

## Validation

- [ ] `mvn -q clean package`
- [ ] Manual runtime check on Paper server
- [ ] `/season` command validated

## Configuration impact

If configuration changed, include before/after snippets:

```yaml
season:
  enabled: true
  length-days: 30
  reminder-minutes: [60, 10, 1]
```

## Integration API impact

If API/integration behavior changed, include registration snippet:

```java
RegisteredServiceProvider<SeasonsApi> rsp = Bukkit.getServicesManager().getRegistration(SeasonsApi.class);
if (rsp != null) {
    SeasonsApi api = rsp.getProvider();
    api.registerIntegration(new MyIntegration());
}
```

## Checklist

- [ ] Public API changes are documented in README
- [ ] Config migration notes included (if needed)
- [ ] Backward compatibility considered
- [ ] Changelog/release notes updated (if applicable)
