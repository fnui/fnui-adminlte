<ul>
<g:each in="\${${varName}.${node.name}}" var="${node.name[0]}">
    <li>
        \${${node.name[0]}}
    </li>
</g:each>
</ul>